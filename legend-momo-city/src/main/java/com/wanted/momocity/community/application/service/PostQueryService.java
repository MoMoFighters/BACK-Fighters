package com.wanted.momocity.community.application.service;

import com.wanted.momocity.auth.domain.model.User;
import com.wanted.momocity.community.application.port.UserInfoPort;
import com.wanted.momocity.community.application.result.PostWithContents;
import com.wanted.momocity.community.application.usecase.PostQueryUseCase;
import com.wanted.momocity.community.domain.exception.CommunityNotFoundException;
import com.wanted.momocity.community.domain.model.Comment;
import com.wanted.momocity.community.domain.model.Post;
import com.wanted.momocity.community.domain.model.PostLike;
import com.wanted.momocity.community.domain.repository.CommentRepository;
import com.wanted.momocity.community.domain.repository.PostLikeRepository;
import com.wanted.momocity.community.domain.repository.PostRepository;
import com.wanted.momocity.community.presentation.api.response.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Slf4j
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class PostQueryService implements PostQueryUseCase {

    private final PostRepository postRepository;
    private final PostLikeRepository postLikeRepository;
    private final CommentRepository commentRepository;
    private final UserInfoPort userInfoPort;
    // 조회수 증가 비동기 처리를 위해 단방향 참조
    // PostCommandService -> PostQueryService 참조 없음
    private final PostCommandService postCommandService;

    /*
     * comment.
     *  게시글 목록 조회
     *  -
     *  [Redis 캐싱]
     *  @Cacheable -> CommunityRedisCacheConfig 의 postsCacheConfiguration 사용
     *  -> Jackson2JsonRedisSerializer<PostListResponse> 로 타입 명시
     *  -> record 타입 직렬화/역직렬화 가능
     *  -
     *  [캐시 키]
     *  "posts::null:0:10" (category:page:size)
     *  -> category null 이면 전체 조회
     */
    @Override
    @Cacheable(value = "posts", key = "#category + ':' + #page + ':' + #size", cacheManager = "redisCacheManager")
    public PostListResponse getPosts(Long userId, String category, int page, int size) {

        PageRequest pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<Post> postPage = postRepository.findAll(category, pageable);

        List<Long> postIds = postPage.getContent().stream()
                .map(Post::getId)
                .toList();

        Map<Long, Long> commentCountMap = postIds.isEmpty()
                ? Map.of()
                : commentRepository.countByPostIds(postIds);

        List<PostListResponse.PostItem> items = postPage.getContent().stream()
                .map(post -> {
                    User user = userInfoPort.findById(post.getUserId())
                            .orElseThrow(() -> new CommunityNotFoundException("사용자를 찾을 수 없습니다."));

                    int commentCount = commentCountMap
                            .getOrDefault(post.getId(), 0L).intValue();

                    return new PostListResponse.PostItem(
                            post.getId(),
                            post.getTitle(),
                            post.getCategory(),
                            post.getViewCount(),
                            post.getLikeCount(),
                            commentCount,
                            post.getThumbnailUrl(),
                            post.getUserId(),
                            user.getName(),
                            user.getProfileImageUrl(),
                            user.getRole().name(),
                            post.getCreatedAt()
                    );
                })
                .toList();

        log.info("[Community] 게시글 목록 조회 완료 | category={}, page={}, size={}",
                category, page, size);

        return new PostListResponse(
                items,
                postPage.getTotalElements(),
                postPage.getTotalPages(),
                page
        );
    }

    // 게시글 단건 조회
    // contents, comments, isMine, isLiked 포함
    // 조회수 증가는 CommandService 에서 처리
    @Override
    public PostDetailResponse getPost(Long userId, Long postId) {
        // 조회수 비동기 증가 -> 응답 반환과 무관하게 별도 스레드에서 처리
        postCommandService.increaseViewCount(postId);

        PostWithContents postWithContents = postRepository.findByIdWithContents(postId)
                .orElseThrow(() -> new CommunityNotFoundException("게시글을 찾을 수 없습니다."));

        Post post = postWithContents.post();

        User author = userInfoPort.findById(post.getUserId())
                .orElseThrow(() -> new CommunityNotFoundException("사용자를 찾을 수 없습니다."));

        // 콘텐츠 목록 조회
        // orderNo 기준 정렬
        List<PostContentResponse> contents = postWithContents.contents().stream()
                .map(c -> new PostContentResponse(
                        c.getType().name(),
                        c.getContent(),
                        c.getImageUrl()
                ))
                .toList();

        boolean isLiked = postLikeRepository
                .findByPostIdAndUserId(postId, userId)
                .isPresent();

        boolean isMine = post.getUserId().equals(userId);

        log.info("[Community] 게시글 단건 조회 완료 | postId={}, userId={}", postId, userId);

        return new PostDetailResponse(
                post.getId(),
                post.getTitle(),
                post.getCategory(),
                post.getViewCount(),
                post.getLikeCount(),
                isLiked,
                isMine,
                post.getUserId(),
                author.getName(),
                author.getProfileImageUrl(),
                author.getRole().name(),
                contents,
                post.getCreatedAt()
        );

    }

    @Override
    public PostCommentResponse getComments(Long userId, Long postId, Long cursor, int size) {

        // 게시글 조회 (작성자 userId 확인용)
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new CommunityNotFoundException("게시글을 찾을 수 없습니다."));

        // 전체 댓글 수 조회 (대댓글 제외)
        int totalCount = commentRepository.countByPostId(postId);

        // 커서 기반 댓글 목록 조회
        List<Comment> comments = commentRepository.findByPostIdWithCursor(postId, cursor, size);

        List<CommentResponse> commentResponses = comments.stream()
                .map(c -> {
                    User commentAuthor = userInfoPort.findById(c.getUserId())
                            .orElseThrow(() -> new CommunityNotFoundException("사용자를 찾을 수 없습니다."));

                    // 대댓글 첫 5개만 조회
                    List<Comment> replies = commentRepository
                            .findRepliesByCommentIdWithCursor(c.getId(), null, 5);

                    // 대댓글 5개 초과 여부 확인
                    // size + 1 개 조회 후 5개 초과 시 hasMoreReplies = true
                    boolean hasMoreReplies = replies.size() == 5 &&
                            !commentRepository.findRepliesByCommentIdWithCursor(
                                    c.getId(), replies.get(replies.size() - 1).getId(), 1
                            ).isEmpty();

                    Long nextReplyCursor = hasMoreReplies
                            ? replies.get(replies.size() - 1).getId()
                            : null;

                    List<ReplyResponse> replyResponses = replies.stream()
                            .map(r -> {
                                User replyAuthor = userInfoPort.findById(r.getUserId())
                                        .orElseThrow(() -> new CommunityNotFoundException("사용자를 찾을 수 없습니다."));
                                return new ReplyResponse(
                                        r.getId(),
                                        r.getUserId(),
                                        r.getContent(),
                                        replyAuthor.getName(),
                                        replyAuthor.getProfileImageUrl(),
                                        replyAuthor.getRole().name(),
                                        r.getUserId().equals(userId),
                                        r.getUserId().equals(post.getUserId()),
                                        r.getCreatedAt()
                                );
                            })
                            .toList();

                    return new CommentResponse(
                            c.getId(),
                            c. getUserId(),
                            c.getContent(),
                            commentAuthor.getName(),
                            commentAuthor.getProfileImageUrl(),
                            commentAuthor.getRole().name(),
                            c.getUserId().equals(userId),
                            c.getUserId().equals(post.getUserId()),
                            c.getCreatedAt(),
                            replyResponses,
                            hasMoreReplies,
                            nextReplyCursor
                    );
                })
                .toList();

        // nextCursor 계산
        // 조회된 댓글 수 == size → 다음 페이지 있음
        Long nextCursor = comments.size() == size
                ? comments.get(comments.size() - 1).getId()
                : null;

        log.info("[Community] 게시글 댓글 조회 완료 | postId={}, totalCount={}", postId, totalCount);

        return new PostCommentResponse(totalCount, commentResponses, nextCursor);

    }

    /*
     * comment.
     *  대댓글 목록 조회
     *  isPostWriter 계산 위해 post 조회 필요
     *  - nextCursor
     *  조회된 대댓글 수 == size -> 다음 페이지 존재
     *  조회된 대댓글 수 < size -> 마지막 페이지 -> NULL
     * */

    @Override
    public PostReplyResponse getReplies(Long userId, Long postId, Long commentId, Long cursor, int size) {
        // 게시글 작성자 확인용
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new CommunityNotFoundException("게시글을 찾을 수 없습니다."));

        // 전체 대댓글 수
        int totalCount = commentRepository
                .findRepliesByCommentIdWithCursor(commentId, null, Integer.MAX_VALUE)
                .size();

        // 커서 기반 대댓글 조회
        List<Comment> replies = commentRepository
                .findRepliesByCommentIdWithCursor(commentId, cursor, size);

        List<ReplyResponse> replyResponses = replies.stream()
                .map(r -> {
                    User replyAuthor = userInfoPort.findById(r.getUserId())
                            .orElseThrow(() -> new CommunityNotFoundException("사용자를 찾을 수 없습니다."));
                    return new ReplyResponse(
                            r.getId(),
                            r.getUserId(),
                            r.getContent(),
                            replyAuthor.getName(),
                            replyAuthor.getProfileImageUrl(),
                            replyAuthor.getRole().name(),
                            r.getUserId().equals(userId),
                            r.getUserId().equals(post.getUserId()),
                            r.getCreatedAt()
                    );
                })
                .toList();

        Long nextCursor = replies.size() == size
                ? replies.get(replies.size() - 1).getId()
                : null;

        log.info("[Community] 대댓글 조회 완료 | commentId={}, totalCount={}", commentId, totalCount);

        return new PostReplyResponse(totalCount, replyResponses, nextCursor);

    }

    // 좋아요 누른 사용자 목록 조회
    // userId 포함 -> 클릭 시 해당 사용자 페이지로 이동
    @Override
    public PostLikeListResponse getLikes(Long postId) {
        List<PostLike> likes = postLikeRepository.findAllByPostId(postId);

        List<PostLikeListResponse.LikeUserItem> users = likes.stream()
                .map(like -> {
                    User user = userInfoPort.findById(like.getUserId())
                            .orElseThrow(() -> new CommunityNotFoundException("사용자를 찾을 수 없습니다."));
                    return new PostLikeListResponse.LikeUserItem(
                            user.getId(),
                            user.getName(),
                            user.getProfileImageUrl(),
                            user.getRole().name()
                    );
                })
                .toList();

        log.info("[Community] 좋아요 목록 조회 완료 | postId={}, totalCount={}", postId, likes.size());

        return new PostLikeListResponse(likes.size(), users);
    }

    // 마이페이지 - 내 게시글 목록
    @Override
    public UserPostListResponse getMyPosts(Long userId, Long cursor, int size) {
        return getUserPostListResponse(userId, cursor, size);
    }

    // 상대방 페이지 - 상대방 게시글 목록
    // targetId 로 조회
    @Override
    public UserPostListResponse getUserPosts(Long targetUserId, Long cursor, int size) {
        return getUserPostListResponse(targetUserId, cursor, size);
    }

    // 페이지 공통 로직
    private UserPostListResponse getUserPostListResponse(Long targetUserId, Long cursor, int size) {

        int totalCount = postRepository.countByUserId(targetUserId);

        List<Post> posts = postRepository.findByUserIdWithCursor(targetUserId, cursor, size);

        List<Long> postIds = posts.stream().map(Post::getId).toList();

        Map<Long, Long> commentCountMap = postIds.isEmpty()
                ? Map.of()
                : commentRepository.countByPostIds(postIds);

        List<UserPostListResponse.UserPostItem> items = posts.stream()
                .map(post -> new UserPostListResponse.UserPostItem(
                        post.getId(),
                        post.getTitle(),
                        post.getCategory(),
                        post.getViewCount(),
                        post.getLikeCount(),
                        commentCountMap.getOrDefault(post.getId(), 0L).intValue(),
                        post.getThumbnailUrl(),
                        post.getCreatedAt()
                ))
                .toList();

        // 조회된 게시글 수 == size ->️ 다음 페이지 존재
        Long nextCursor = posts.size() == size
                // cursor = null -> 첫 페이지
                // cursor != null -> 해당 postId 이후 데이터 조회
                ? posts.get(posts.size() - 1).getId()
                // 조회된 게시글 수 < size -> 마지막 페이지 -> null
                : null;

        log.info("[Community] 유저 게시글 목록 조회 완료 | targetUserId={}, totalCount={}",
                targetUserId, totalCount);

        return new UserPostListResponse(totalCount, items, nextCursor);
    }


    // 대시보드 - 내 게시글 통계
    @Override
    public DashboardResponse getDashboard(Long userId) {

        int totalPostCount = postRepository.countByUserId(userId);
        int totalViewCount = postRepository.sumViewCountByUserId(userId);
        int totalLikeCount = postRepository.sumLikeCountByUserId(userId);

        // 내 게시글 postId 목록
        List<Post> myPosts = postRepository.findByUserIdWithCursor(userId, null, Integer.MAX_VALUE);
        List<Long> postIds = myPosts.stream().map(Post::getId).toList();

        // 총 댓글 수 (대댓글 제외)
        int totalCommentCount = postIds.isEmpty()
                ? 0
                : commentRepository.countByPostIds(postIds)
                  .values().stream()
                  .mapToInt(Long::intValue)
                  .sum();


        log.info("[Community] 대시보드 조회 완료 | userId={}", userId);

        return new DashboardResponse(
                totalPostCount,
                totalViewCount,
                totalLikeCount,
                totalCommentCount
        );

    }
}
