package com.wanted.momocity.community.application.post.service;

import com.wanted.momocity.auth.domain.model.User;
import com.wanted.momocity.community.application.post.port.UserInfoPort;
import com.wanted.momocity.community.application.post.result.PostWithContents;
import com.wanted.momocity.community.application.post.usecase.PostQueryUseCase;
import com.wanted.momocity.community.domain.exception.CommunityNotFoundException;
import com.wanted.momocity.community.domain.model.Post;
import com.wanted.momocity.community.domain.model.PostCategory;
import com.wanted.momocity.community.domain.repository.CommentRepository;
import com.wanted.momocity.community.domain.repository.PostLikeRepository;
import com.wanted.momocity.community.domain.repository.PostRepository;
import com.wanted.momocity.community.infrastructure.metrics.CommunityMetrics;
import com.wanted.momocity.community.presentation.api.response.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
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
    private final CommunityMetrics communityMetrics;

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
    @Cacheable(
            value = "posts",
            key = "#category + ':' + #cursor + ':' + #size",
            cacheManager = "redisCacheManager"
    )
    public PostListResponse getPosts(Long userId, PostCategory category, Long cursor, int size) {

        // 전체 게시글 수 조회 (카테고리 필터 적용)
        int totalCount = postRepository.countByCategory(category);

        // 커서 기반 게시글 목록 조회
        // 커서 페이지네이션 Timer
        List<Post> posts = communityMetrics.getPostListQueryTimer().record(
                () -> postRepository.findAllWithCursor(category, cursor, size)
        );

        // nextCursor 계산
        // size + 1 개 조회 후 실제 반환 = size 개 -> size + 1 번째 데이터 존재 = 다음 페이지 존재, 없으면 null 반환
        // size 개 조회 시 마지막 페이지일 시 nextCursor 반환 -> 빈페이지 요청 발생
        boolean hasNext = posts.size() > size;
        List<Post> pagedPosts = hasNext ? posts.subList(0, size) : posts;
        Long nextCursor = hasNext ? pagedPosts.get(pagedPosts.size() - 1).getId() : null;

        // postId 목록 추출 → 댓글 수 일괄 조회용
        List<Long> postIds = posts.stream()
                .map(Post::getId)
                .toList();

        // N+1 개선 : 댓글 수 한 번에 조회
        // postId 목록으로 GROUP BY 쿼리 -> Map(postId, count)
        Map<Long, Long> commentCountMap = postIds.isEmpty()
                ? Map.of()
                : commentRepository.countByPostIds(postIds);

        // 게시글 목록 → PostItem 변환
        List<PostListResponse.PostItem> items = pagedPosts.stream()
                .map(post -> {
                    User user = userInfoPort.findById(post.getUserId())
                            .orElseThrow(() -> new CommunityNotFoundException("사용자를 찾을 수 없습니다."));

                    // postId 기준 댓글 수 조회 (Map 에서 없으면 0 반환)
                    int commentCount = commentCountMap
                            .getOrDefault(post.getId(), 0L).intValue();

                    return new PostListResponse.PostItem(
                            post.getId(),
                            post.getTitle(),
                            post.getCategory().name(),
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

        log.info("[Community] 게시글 목록 조회 완료 | category={}, cursor={}, size={}",
                category, cursor, size);

        return new PostListResponse(totalCount, items, nextCursor);
    }

    // 게시글 단건 조회
    @Override
    public PostDetailResponse getPost(Long userId, Long postId) {

        // 조회수 비동기 증가 -> 응답 반환과 무관하게 별도 스레드에서 처리
        postCommandService.increaseViewCount(postId);

        // 게시글 + 콘텐츠 fetch join 조회
        PostWithContents postWithContents = postRepository.findByIdWithContents(postId)
                .orElseThrow(() -> new CommunityNotFoundException("게시글을 찾을 수 없습니다."));

        Post post = postWithContents.post();

        // 게시글 작성자 정보 조회
        User author = userInfoPort.findById(post.getUserId())
                .orElseThrow(() -> new CommunityNotFoundException("사용자를 찾을 수 없습니다."));

        // 콘텐츠 목록 조회 (orderNo 기준 정렬)
        List<PostContentResponse> contents = postWithContents.contents().stream()
                .map(c -> new PostContentResponse(
                        c.getType().name(),
                        c.getContent(),
                        c.getImageUrl()
                ))
                .toList();

        // EXISTS 쿼리로 boolean 만 반환 -> 불필요한 데이터 로드 방지
        boolean isLiked = postLikeRepository.existsByPostIdAndUserId(postId, userId);

        // 현재 로그인 유저가 게시글 작성자인지 확인
        boolean isMine = post.getUserId().equals(userId);

        log.info("[Community] 게시글 단건 조회 완료 | postId={}, userId={}", postId, userId);

        return new PostDetailResponse(
                post.getId(),
                post.getTitle(),
                post.getCategory().name(),
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

        // 유저별 전체 게시글 수 조회 (소프트딜리트 제외)
        int totalCount = postRepository.countByUserId(targetUserId);

        // 커서 기반 유저별 게시글 목록 조회 (size + 1 개 조회 -> 다음 페이지 존재 여부 확인용)
        List<Post> posts = postRepository.findByUserIdWithCursor(targetUserId, cursor, size);

        // postId 목록 추출 -> 댓글 수 일괄 조회용
        List<Long> postIds = posts.stream().map(Post::getId).toList();

        // N + 1 개선 : 댓글 수 한 번에 조회 (postIds 비어있으면 빈 Map 반환)
        Map<Long, Long> commentCountMap = postIds.isEmpty()
                ? Map.of()
                : commentRepository.countByPostIds(postIds);

        // nextCursor 계산
        // size + 1 개 조회 후 실제 반환 = size 개 -> size + 1 번째 데이터 존재 = 다음 페이지 존재, 없으면 null 반환
        // size 개 조회 시 마지막 페이지일 시 nextCursor 반환 -> 빈페이지 요청 발생
        boolean hasNext = posts.size() > size;
        List<Post> pagedPosts = hasNext ? posts.subList(0, size) : posts;
        Long nextCursor = hasNext ? pagedPosts.get(pagedPosts.size() - 1).getId() : null;

        // items 변환 -> pagedPosts 사용
        List<UserPostListResponse.UserPostItem> items = pagedPosts.stream()
                .map(post -> {
                    // 사용자 정보 조회
                    User user = userInfoPort.findById(post.getUserId())
                            .orElseThrow(() -> new CommunityNotFoundException("사용자를 찾을 수 없습니다."));
                    return new UserPostListResponse.UserPostItem(
                            post.getId(),
                            post.getTitle(),
                            post.getCategory().name(),
                            post.getViewCount(),
                            post.getLikeCount(),
                            commentCountMap.getOrDefault(post.getId(), 0L).intValue(),
                            post.getThumbnailUrl(),
                            post.getUserId(),
                            user.getName(),
                            user.getProfileImageUrl(),
                            user.getRole().name(),
                            post.getCreatedAt()
                    );
                })
                .toList();

        log.info("[Community] 유저 게시글 목록 조회 완료 | targetUserId={}, totalCount={}",
                targetUserId, totalCount);

        return new UserPostListResponse(totalCount, items, nextCursor);
    }


    // 대시보드 - 내 게시글 통계
    @Override
    public DashboardResponse getDashboard(Long userId) {

        // 유저별 전체 게시글 수 조회
        int totalPostCount = postRepository.countByUserId(userId);
        // 유저별 총 조회수 합산
        int totalViewCount = postRepository.sumViewCountByUserId(userId);
        // 유저별 총 좋아요 수 합산
        int totalLikeCount = postRepository.sumLikeCountByUserId(userId);

        // postIds 만 조회
        List<Long> postIds = postRepository.findPostIdsByUserId(userId);

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

    // 커뮤니티 게시글 검색
    @Override
    public UserPostListResponse searchPosts(String keyword, PostCategory category, Long cursor, int size) {

        // 키워드 + 카테고리 기준 전체 검색 결과 수 조회
        int totalCount = postRepository.countByKeyword(keyword, category);

        // 커서 기반 키워드 검색 (size + 1개 조회 -> 다음 페이지 존재 여부 확인용)
        // 검색 쿼리 Timer
        List<Post> posts = communityMetrics.getPostSearchQueryTimer().record(
                () -> postRepository.searchByKeyword(keyword, category, cursor, size)
        );

        // postId 목록 추출 -> 댓글 수 일괄 조회용
        List<Long> postIds = posts.stream().map(Post::getId).toList();

        // N + 1 개선 : 댓글 수 한 번에 조회 (postIds 비어잇으면 빈 Map 조회)
        Map<Long, Long> commentCountMap = postIds.isEmpty()
                ? Map.of()
                : commentRepository.countByPostIds(postIds);

        // nextCursor 계산
        // size + 1 개 조회 후 실제 반환 = size 개 -> size + 1 번째 데이터 존재 = 다음 페이지 존재, 없으면 null 반환
        // size 개 조회 시 마지막 페이지일 시 nextCursor 반환 -> 빈페이지 요청 발생
        boolean hasNext = posts.size() > size;
        List<Post> pagedPosts = hasNext ? posts.subList(0, size) : posts;
        Long nextCursor = hasNext ? pagedPosts.get(pagedPosts.size() - 1).getId() : null;

        // items 변환 -> pagedPosts 사용
        List<UserPostListResponse.UserPostItem> items = pagedPosts.stream()
                .map(post -> {
                    // 사용자 정보 조회
                    User user = userInfoPort.findById(post.getUserId())
                            .orElseThrow(() -> new CommunityNotFoundException("사용자를 찾을 수 없습니다."));
                    return new UserPostListResponse.UserPostItem(
                            post.getId(),
                            post.getTitle(),
                            post.getCategory().name(),
                            post.getViewCount(),
                            post.getLikeCount(),
                            commentCountMap.getOrDefault(post.getId(), 0L).intValue(),
                            post.getThumbnailUrl(),
                            post.getUserId(),
                            user.getName(),
                            user.getProfileImageUrl(),
                            user.getRole().name(),
                            post.getCreatedAt()
                    );
                })
                .toList();

        log.info("[Community] 게시글 검색 완료 | keyword={}, totalCount={}", keyword, totalCount);

        return new UserPostListResponse(totalCount, items, nextCursor);
    }

    // 연관 게시글 추천
    @Override
    public PostRecommendationResponse getRecommendations(Long postId) {

        // 현재 게시글 조회
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new CommunityNotFoundException("게시글을 찾을 수 없습니다."));

        // 같은 카테고리 인기 게시글 3개 조회
        List<Post> topPosts = postRepository.findTopPostsByCategory(
                post.getCategory(), postId, 3
        );

        // topPosts postId 목록 (authorPosts 중복 제외용)
        List<Long> topPostIds = topPosts.stream()
                .map(Post::getId)
                .toList();

        // 같은 작성자 최신 게시글 2개 조회 (topPosts 중복 제외)
        List<Post> authorPosts = postRepository.findLatestPostsByAuthor(
                post.getUserId(), postId, topPostIds, 2
        );

        // topPosts -> RecommendItem 변환
        List<PostRecommendationResponse.RecommendItem> topItems = topPosts.stream()
                .map(p -> {
                    // 사용자 정보 조회
                    User author = userInfoPort.findById(p.getUserId())
                            .orElseThrow(() -> new CommunityNotFoundException("사용자를 찾을 수 없습니다."));
                    return new PostRecommendationResponse.RecommendItem(
                            p.getId(),
                            p.getTitle(),
                            p.getCategory().name(),
                            p.getViewCount(),
                            p.getLikeCount(),
                            p.getThumbnailUrl(),
                            p.getUserId(),
                            author.getName(),
                            p.getCreatedAt()
                    );
                })
                .toList();

        // authorPosts -> RecommendItem 변환
        List<PostRecommendationResponse.RecommendItem> authorItems = authorPosts.stream()
                .map(p -> {
                    // 사용자 정보 조회
                    User author = userInfoPort.findById(p.getUserId())
                            .orElseThrow(() -> new CommunityNotFoundException("사용자를 찾을 수 없습니다."));
                    return new PostRecommendationResponse.RecommendItem(
                            p.getId(),
                            p.getTitle(),
                            p.getCategory().name(),
                            p.getViewCount(),
                            p.getLikeCount(),
                            p.getThumbnailUrl(),
                            p.getUserId(),
                            author.getName(),
                            p.getCreatedAt()
                    );
                })
                .toList();

        log.info("[Community] 연관 게시글 추천 완료 | postId={}, topCount={}, authorCount={}",
                postId, topItems.size(), authorItems.size());

        return new PostRecommendationResponse(topItems, authorItems);

    }
}
