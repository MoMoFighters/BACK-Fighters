package com.wanted.momocity.community.application.service;

import com.wanted.momocity.auth.domain.model.User;
import com.wanted.momocity.community.application.port.UserInfoPort;
import com.wanted.momocity.community.application.usecase.PostQueryUseCase;
import com.wanted.momocity.community.domain.exception.CommunityNotFoundException;
import com.wanted.momocity.community.domain.model.Comment;
import com.wanted.momocity.community.domain.model.Post;
import com.wanted.momocity.community.domain.repository.CommentRepository;
import com.wanted.momocity.community.domain.repository.PostContentRepository;
import com.wanted.momocity.community.domain.repository.PostLikeRepository;
import com.wanted.momocity.community.domain.repository.PostRepository;
import com.wanted.momocity.community.presentation.api.response.CommentResponse;
import com.wanted.momocity.community.presentation.api.response.PostContentResponse;
import com.wanted.momocity.community.presentation.api.response.PostDetailResponse;
import com.wanted.momocity.community.presentation.api.response.PostListResponse;
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
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PostQueryService implements PostQueryUseCase {

    private final PostRepository postRepository;
    private final PostContentRepository postContentRepository;
    private final PostLikeRepository postLikeRepository;
    private final CommentRepository commentRepository;
    private final UserInfoPort userInfoPort;
    // 조회수 증가 비동기 처리를 위해 단방향 참조
    // PostCommandService -> PostQueryService 참조 없음
    private final PostCommandService postCommandService;

    // 게시글 목록 조회
    // category 없으면 전체 조회
    // 페이지 네이션 적용
    // authorName. authorProfileImageUrl 포함함
    @Override
    @Cacheable(value = "posts", key = "#category + ':' + #page + ':' + #size")
    public PostListResponse getPosts(Long userId, String category, int page, int size) {
        PageRequest pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<Post> postPage = postRepository.findAll(category, pageable);

        List<PostListResponse.PostItem> items = postPage.getContent().stream()
                .map(post -> {
                    User user = userInfoPort.findById(post.getUserId())
                            .orElseThrow(() -> new CommunityNotFoundException("사용자를 찾을 수 없습니다."));

                    // commentCount : 해당 게시글 댓글 + 대댓글 수
                    int commentCount = commentRepository.findAllByPostId(post.getId()).size();

                    return new PostListResponse.PostItem(
                            post.getId(),
                            post.getTitle(),
                            post.getCategory(),
                            post.getViewCount(),
                            post.getLikeCount(),
                            commentCount,
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
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new CommunityNotFoundException("게시글을 찾을 수 없습니다."));

        User author = userInfoPort.findById(post.getUserId())
                .orElseThrow(() -> new CommunityNotFoundException("사용자를 찾을 수 없습니다."));

        // 콘텐츠 목록 조회
        // orderNo 기준 정렬
        List<PostContentResponse> contents = postContentRepository
                .findAllByPostId(postId)
                .stream()
                .map(c -> new PostContentResponse(
                        c.getOrderNo(),
                        c.getType().name(),
                        c.getContent(),
                        c.getImageUrl()
                ))
                .toList();

        // 댓글 목록 조회
        // 댓글 (parentId = null) + 대댓글 (parent != null) 분리
        // 댓글에 대댓글 묶어서 반환
        List<Comment> allComments = commentRepository.findAllByPostId(postId);

        Map<Long, List<Comment>> repliesMap = allComments.stream()
                .filter(Comment::isReply)
                .collect(Collectors.groupingBy(Comment::getParentId));

        List<CommentResponse> comments = allComments.stream()
                .filter(c -> !c.isReply())
                .map(c -> {
                    User commentAuthor = userInfoPort.findById(c.getUserId())
                            .orElseThrow(() -> new CommunityNotFoundException("사용자를 찾을 수 없습니다."));

                    List<CommentResponse> replies = repliesMap
                            .getOrDefault(c.getId(), List.of())
                            .stream()
                            .map(r -> {
                                User replyAuthor = userInfoPort.findById(r.getUserId())
                                        .orElseThrow(() -> new CommunityNotFoundException("사용자를 찾을 수 없습니다."));
                                return new CommentResponse(
                                        r.getId(),
                                        r.getContent(),
                                        replyAuthor.getName(),
                                        replyAuthor.getProfileImageUrl(),
                                        replyAuthor.getRole().name(),
                                        r.getCreatedAt(),
                                        List.of()
                                );
                            })
                            .toList();

                    return new CommentResponse(
                            c.getId(),
                            c.getContent(),
                            commentAuthor.getName(),
                            commentAuthor.getProfileImageUrl(),
                            commentAuthor.getRole().name(),
                            c.getCreatedAt(),
                            replies
                    );
                })
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
                author.getName(),
                author.getProfileImageUrl(),
                author.getRole().name(),
                contents,
                comments,
                post.getCreatedAt()
        );

    }
}
