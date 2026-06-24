package com.wanted.momocity.community.application.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.wanted.momocity.auth.domain.model.User;
import com.wanted.momocity.community.application.port.UserInfoPort;
import com.wanted.momocity.community.application.result.PostWithContents;
import com.wanted.momocity.community.application.usecase.PostQueryUseCase;
import com.wanted.momocity.community.domain.exception.CommunityNotFoundException;
import com.wanted.momocity.community.domain.model.Comment;
import com.wanted.momocity.community.domain.model.Post;
import com.wanted.momocity.community.domain.repository.CommentRepository;
import com.wanted.momocity.community.domain.repository.PostContentRepository;
import com.wanted.momocity.community.domain.repository.PostLikeRepository;
import com.wanted.momocity.community.domain.repository.PostRepository;
import com.wanted.momocity.community.presentation.api.response.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
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

    // String 타입 전용 RedisTemplate
    // 저장 / 조회 시 raw JSON 문자열 그대로 처리
    // GenericJackson2JsonRedisSerializer 의 @class 타입 정보 충돌 문제 없음
    private final StringRedisTemplate stringRedisTemplate;

    // activateDefaultTyping 비활성화 상태의 순수 ObjectMapper
    // @class 타입 정보 없이 순수 JSON 으로 직렬화 / 역직렬화
    // JavaTimeModule 등록으로 LocalDateTime 처리 가능
    // HTTP 응답 직렬화에는 영향 없음 (Spring 기본 ObjectMapper 와 별개)
    private final ObjectMapper plainObjectMapper = new ObjectMapper()
            .registerModule(new com.fasterxml.jackson.datatype.jsr310.JavaTimeModule());

    public PostQueryService(
            PostRepository postRepository,
            PostContentRepository postContentRepository,
            PostLikeRepository postLikeRepository,
            CommentRepository commentRepository,
            UserInfoPort userInfoPort,
            PostCommandService postCommandService,
            StringRedisTemplate stringRedisTemplate
    ) {
        this.postRepository = postRepository;
        this.postContentRepository = postContentRepository;
        this.postLikeRepository = postLikeRepository;
        this.commentRepository = commentRepository;
        this.userInfoPort = userInfoPort;
        this.postCommandService = postCommandService;
        this.stringRedisTemplate = stringRedisTemplate;
    }

    /*
    * comment.
    *  게시글 목록 조회
    *  캐시 키
    *  - "posts::null:0:10" (category, page, size) -> category = null : 전체 조회
    *  -
    *  캐시 조회 우선
    *  1. Redis 에서 키로 raw JSON 조회
    *  2. 캐시 히트 시 plainObjectMapper 로 PostListResponse 역직렬화 후 반환
    *  3. 캐시 미스 시 DB 조회 후 Redis 에 저장
    *  -
    *  예외처리
    *  - 캐시 조회 / 저장 실패 시 -> 로그만 남기고 DB 조회로 fallback
    *  -> 캐시 장애가 서비스 장애로 이어지지 않도록 방어
    * */
    @Override
    public PostListResponse getPosts(Long userId, String category, int page, int size) {

        String cacheKey = "posts::" + category + ":" + page + ":" + size;

        // 1. Redis 캐시 조회
        try {
            String json = stringRedisTemplate.opsForValue().get(cacheKey);
            if (json != null) {
                PostListResponse cached = plainObjectMapper.readValue(
                        json, new TypeReference<PostListResponse>() {
                        }
                );
                log.debug("[Community] posts 캐시 히트 | key={}", cacheKey);
                return cached;
            }
        } catch (Exception e) {
            log.warn("[Community] posts 캐시 조회 실패, DB 조회로 fallback | key={} | 예외={}",
                    cacheKey, e.getMessage());
        }

        // 2. DB 조회
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

        PostListResponse response = new PostListResponse(
                items,
                postPage.getTotalElements(),
                postPage.getTotalPages(),
                page
        );

        log.info("[Community] 게시글 목록 조회 완료 | category={}, page={}, size={}",
                category, page, size);

        // 3. Redis 캐시 저장
        // TTL 1시간 → 게시글 작성/수정/삭제 시 @CacheEvict 로 무효화
        try {
            String json = plainObjectMapper.writeValueAsString(response);
            stringRedisTemplate.opsForValue().set(cacheKey, json, Duration.ofHours(1));
            log.debug("[Community] posts 캐시 저장 | key={}", cacheKey);
        } catch (Exception e) {
            log.warn("[Community] posts 캐시 저장 실패 | key={}", cacheKey);
        }

        return response;
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

                    List<Comment> replies = commentRepository
                            .findAllByPostId(postId)
                            .stream()
                            .filter(r -> r.isReply() && r.getParentId().equals(c.getId()))
                            .toList();

                    List<CommentResponse> replyResponses = replies.stream()
                            .map(r -> {
                                User replyAuthor = userInfoPort.findById(r.getUserId())
                                        .orElseThrow(() -> new CommunityNotFoundException("사용자를 찾을 수 없습니다."));
                                return new CommentResponse(
                                        r.getId(),
                                        r.getUserId(),
                                        r.getContent(),
                                        replyAuthor.getName(),
                                        replyAuthor.getProfileImageUrl(),
                                        replyAuthor.getRole().name(),
                                        r.getUserId().equals(userId),
                                        r.getUserId().equals(post.getUserId()),
                                        r.getCreatedAt(),
                                        List.of()
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
                            replyResponses
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
}
