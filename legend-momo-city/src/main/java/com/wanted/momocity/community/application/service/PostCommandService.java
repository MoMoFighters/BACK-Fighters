package com.wanted.momocity.community.application.service;

import com.wanted.momocity.auth.domain.model.User;
import com.wanted.momocity.community.application.command.PostContentCommand;
import com.wanted.momocity.community.application.port.UserInfoPort;
import com.wanted.momocity.community.application.result.LikeResult;
import com.wanted.momocity.community.application.result.PostCreateResult;
import com.wanted.momocity.community.application.usecase.PostCommandUseCase;
import com.wanted.momocity.community.domain.event.CommentCreatedEvent;
import com.wanted.momocity.community.domain.event.PostLikedEvent;
import com.wanted.momocity.community.domain.event.ReplyCreatedEvent;
import com.wanted.momocity.community.domain.exception.CommunityAccessDeniedException;
import com.wanted.momocity.community.domain.exception.CommunityNotFoundException;
import com.wanted.momocity.community.domain.model.*;
import com.wanted.momocity.community.domain.repository.CommentRepository;
import com.wanted.momocity.community.domain.repository.PostContentRepository;
import com.wanted.momocity.community.domain.repository.PostLikeRepository;
import com.wanted.momocity.community.domain.repository.PostRepository;
import com.wanted.momocity.community.infrastructure.metrics.CommunityMetrics;
import com.wanted.momocity.global.application.s3.S3UploadPort;
import com.wanted.momocity.global.domain.common.exception.DomainRuleViolationException;
import com.wanted.momocity.global.infrastructure.cloudfront.CloudFrontUrlConverter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;

/*
* comment.
*  게시글 쓰기 작업 UseCase 구현체
*  - 게시글 생성, 수정, 삭제
*  - 콘텐츠 업로드, 수정
*  - 좋아요, 댓글, 대댓글
* */

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class PostCommandService implements PostCommandUseCase {

    private final PostRepository postRepository;
    private final PostContentRepository postContentRepository;
    private final PostLikeRepository postLikeRepository;
    private final CommentRepository commentRepository;
    private final UserInfoPort userInfoPort;
    private final CloudFrontUrlConverter cloudFrontUrlConverter;
    private final S3UploadPort s3UploadPort;
    private final ApplicationEventPublisher eventPublisher;
    private final CommunityMetrics communityMetrics;

    // 게시글 생성
    // title, category 만 저장 -> postId 반환 후 콘텐츠 업로드 API 호출
    // 게시글 추가 시 목록 캐시 전체 무효화
    @Override
    @CacheEvict(value = "posts", allEntries = true, cacheManager = "redisCacheManager")
    public PostCreateResult createPost(Long userId, String title, PostCategory category, String thumbnailUrl) {

        // 게시글 생성
        Post post = Post.create(userId, title, category, thumbnailUrl);
        // 게시글 저장
        Post saved = postRepository.save(post);

        // 게시글 작성 횟수 카운트
        communityMetrics.recordPostCreated();

        log.info("[Community] 게시글 생성 완료 | userId={}, postId={}", userId, saved.getId());
        return new PostCreateResult(saved.getId());
    }

    // 게시글 콘텐츠 업로드 (POST)
    // 최초 작성 시 호출 -> IMAGE 타입 최대 5개 검증, order_no 자동 부여
    // 캐시 무효화 불필요
    // 컨텐츠 업로드는 목록 조회에 영향 없음 -> PostListResponse 에 contents 미포함
    @Override
    public void uploadContents(Long userId, Long postId, String thumbnailUrl, List<PostContentCommand> contents) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new CommunityNotFoundException("게시글을 찾을 수 없습니다."));

        // 게시글 작성자 본인 검증
        validateAuthor(post.getUserId(), userId);
        // 이미지 개수 검증 (최대 이미지 개수 초과 방지)
        validateImageCount(contents);
        // 콘텐츠 타입별 필수값 검증 (TEXT -> content, IMAGE -> imageUrl)
        validateContents(contents);

        post.updateThumbnail(thumbnailUrl);
        postRepository.save(post);

        // 새 컨텐츠 목록 생성
        List<PostContent> postContents = new ArrayList<>();
        for (int i = 0; i < contents.size(); i++) {
            PostContentCommand cmd = contents.get(i);
            postContents.add(PostContent.create(
                    postId,
                    i + 1,  // orderNo 자동 부여
                    PostContent.Type.valueOf(cmd.type()),
                    cmd.content(),
                    cmd.imageUrl()
            ));
        }
        postContentRepository.saveAll(postContents);
        log.info("[Community] 콘텐츠 업로드 완료 | postId={}, count={}", postId, postContents.size());
    }

    // 게시글 제목 / 카테고리 수정
    // 제목 / 카테고리 수정 시 목록 캐시 전체 무효화
    @Override
    @CacheEvict(value = "posts", allEntries = true, cacheManager = "redisCacheManager")
    public void updatePost(Long userId, Long postId, String title, PostCategory category) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new CommunityNotFoundException("게시글을 찾을 수 없습니다."));

        validateAuthor(post.getUserId(), userId);
        post.update(title, category);
        postRepository.save(post);

        log.info("[Community] 게시글 수정 완료 | postId={}", postId);
    }

    // 게시글 콘텐츠 수정 (PUT)
    // 기준 콘텐츠 전체 소프트딜리트 후 새 콘텐츠 저장 -> 트랜잭션으로 한번에 처리
    @Override
    @CacheEvict(value = "posts", allEntries = true, cacheManager = "redisCacheManager")
    public void updateContents(Long userId, Long postId, String thumbnailUrl, List<PostContentCommand> contents) {

        // 게시글 존재 여부 확인
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new CommunityNotFoundException("게시글을 찾을 수 없습니다."));

        // 게시글 작성자 본인 검증
        validateAuthor(post.getUserId(), userId);
        // 이미지 개수 검증 (최대 이미지 개수 초과 방지)
        validateImageCount(contents);
        // 콘텐츠 타입별 필수값 검증 (TEXT -> content, IMAGE -> imageUrl)
        validateContents(contents);

        // 썸네일 업데이트
        post.updateThumbnail(thumbnailUrl);
        postRepository.save(post);

        // 기존 콘텐츠 전체 소프트딜리트 -> 새 콘텐츠 저장
        postContentRepository.deleteAllByPostId(postId);

        // 새 컨텐츠 목록 생성
        List<PostContent> postContents = new ArrayList<>();
        for (int i = 0; i < contents.size(); i++) {
            PostContentCommand cmd = contents.get(i);
            postContents.add(PostContent.create(
                    postId,
                    i + 1,  // orderNo 자동 부여
                    PostContent.Type.valueOf(cmd.type()),
                    cmd.content(),
                    cmd.imageUrl()
            ));

        }

        // 새 컨텐츠 일괄 저장
        postContentRepository.saveAll(postContents);
        log.info("[Community] 콘텐츠 수정 완료 | postId={}, count={}", postId, postContents.size());
    }

    // 게시글 삭제
    // post 소프트딜리트 -> post_content 소프트딜리트
    // 게시글 삭제 시 목록 캐시 전체 무효화
    @Override
    @CacheEvict(value = "posts", allEntries = true, cacheManager = "redisCacheManager")
    public void deletePost(Long userId, Long postId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new CommunityNotFoundException("게시글을 찾을 수 없습니다."));

        validateAuthor(post.getUserId(), userId);
        post.delete();
        postRepository.save(post);
        postContentRepository.deleteAllByPostId(postId);

        log.info("[Community] 게시글 삭제 완료 | postId={}", postId);
    }

    // 좋아요
    // post_like 저장 -> post.like_count +1
    @Override
    public LikeResult likePost(Long userId, Long postId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new CommunityNotFoundException("게시글을 찾을 수 없습니다."));

        // 삭제된 게시글 좋아요 방지
        if (post.isDeleted()) {
            throw new CommunityAccessDeniedException("삭제된 게시글에는 좋아요를 누를 수 없습니다.");
        }

        postLikeRepository.findByPostIdAndUserId(postId, userId)
                .ifPresent(like -> {
                    throw new CommunityAccessDeniedException("이미 좋아요를 눌렀습니다.");
                });

        postLikeRepository.save(PostLike.create(postId, userId));
        post.increaseLikeCount();
        postRepository.save(post);

        // 좋아요 횟수 카운트
        communityMetrics.recordPostLiked();

        // 본인 게시글 좋아요 시 알림 제외
        if (!post.getUserId().equals(userId)) {
            String likerName = userInfoPort.findById(userId)
                    .orElseThrow(() -> new CommunityNotFoundException("사용자를 찾을 수 없습니다."))
                    .getName();
            eventPublisher.publishEvent(
                    new PostLikedEvent(postId, post.getUserId(), userId, likerName));
        }

        log.info("[Community] 좋아요 완료 | userId={}, postId={}", userId, postId);
        return new LikeResult(postId, post.getLikeCount(), true);
    }

    // 좋아요 취소
    // post_like 삭제 -> post.like_count -1
    @Override
    public LikeResult unlikePost(Long userId, Long postId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new CommunityNotFoundException("게시글을 찾을 수 없습니다."));

        PostLike postLike = postLikeRepository.findByPostIdAndUserId(postId, userId)
                .orElseThrow(() -> new CommunityNotFoundException("좋아요를 누르지 않았습니다."));

        postLikeRepository.delete(postLike);
        post.decreaseLikeCount();
        postRepository.save(post);

        log.info("[Community] 좋아요 취소 완료 | userId={}, postId={}", userId, postId);
        return new LikeResult(postId, post.getLikeCount(), false);
    }

    // 댓글 작성
    @Override
    public void createComment(Long userId, Long postId, String content) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new CommunityNotFoundException("게시글을 찾을 수 없습니다."));

        if (post.isDeleted()) {
            throw new CommunityAccessDeniedException("삭제된 게시글에는 댓글을 작성할 수 없습니다.");
        }

        Comment comment = Comment.create(postId, userId, content);
        Comment saved = commentRepository.save(comment);

        // 유저 정보 조회 (알림 + Result 공통 사용)
        User user = userInfoPort.findById(userId)
                .orElseThrow(() -> new CommunityNotFoundException("사용자를 찾을 수 없습니다."));

        // 본인 게시글 댓글 시 알림 제외
        if (!post.getUserId().equals(userId)) {
            eventPublisher.publishEvent(
                    new CommentCreatedEvent(postId, post.getUserId(), userId, user.getName()));
        }

        log.info("[Community] 댓글 작성 완료 | userId={}, postId={}, commentId={}",
                userId, postId, saved.getId());
    }

    // 댓글 삭제
    // 소프트딜리트 -> 대댓글도 소프트딜리트
    @Override
    public void deleteComment(Long userId, Long postId, Long commentId) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new CommunityNotFoundException("댓글을 찾을 수 없습니다."));

        // 해당 게시글 소속 댓글인지 검증
        // -> 다른 게시글 댓글 삭제 시도 방지
        if (!comment.getPostId().equals(postId)) {
            throw new CommunityAccessDeniedException("해당 게시글의 댓글이 아닙니다.");
        }

        validateAuthor(comment.getUserId(), userId);
        comment.delete();
        commentRepository.delete(comment);

        log.info("[Community] 댓글 삭제 완료 | commentId={}", commentId);
    }

    // 대댓글 작성
    // parentId = commentId -> 대댓글에 대댓글 불가
    @Override
    public void createReply(Long userId, Long postId, Long commentId, String content) {
        Comment parentComment = commentRepository.findById(commentId)
                .orElseThrow(() -> new CommunityNotFoundException("댓글을 찾을 수 없습니다."));

        if (parentComment.isReply()) {
            throw new CommunityAccessDeniedException("대댓글에는 답글을 달 수 없습니다.");
        }

        if (parentComment.isDeleted()) {
            throw new CommunityAccessDeniedException("삭제된 댓글에는 대댓글을 작성할 수 없습니다.");
        }

        Comment reply = Comment.createReply(postId, userId, commentId, content);
        Comment saved = commentRepository.save(reply);

        User user = userInfoPort.findById(userId)
                .orElseThrow(() -> new CommunityNotFoundException("사용자를 찾을 수 없습니다."));

        // postOwnerId 조회
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new CommunityNotFoundException("게시글을 찾을 수 없습니다."));

        // 본인 게시글 또는 본인 댓글에 대댓글 시 알림 제외
        if (!userId.equals(post.getUserId()) && !userId.equals(parentComment.getUserId())) {
            eventPublisher.publishEvent(new ReplyCreatedEvent(
                    postId,
                    post.getUserId(),
                    parentComment.getUserId(),
                    userId,
                    user.getName()
            ));
        }

        log.info("[Community] 대댓글 작성 완료 | userId={}, commentId={}, replyId={}",
                userId, commentId, saved.getId());

    }

    // 대댓글 삭제
    // 소프트딜리트
    @Override
    public void deleteReply(Long userId, Long postId, Long commentId, Long replyId) {

        // 대댓글 존재 검증
        Comment reply = commentRepository.findById(replyId)
                .orElseThrow(() -> new CommunityNotFoundException("대댓글을 찾을 수 없습니다."));

        // 해당 게시글 소속 대댓글인지 검증
        if (!reply.getPostId().equals(postId)) {
            throw new CommunityAccessDeniedException("해당 게시글의 대댓글이 아닙니다.");
        }

        validateAuthor(reply.getUserId(), userId);
        reply.delete();
        commentRepository.delete(reply);

        log.info("[Community] 대댓글 삭제 완료 | replyId={}", replyId);
    }

    // 조회수 증가
    // @Async : 별도 스레드에서 실행 -> 응답 속도에 영향 없음
    // domainEventExecutor : AsyncConfig 에 등록된 스레드를 재사용
    // @Transactional : 조회수 증가는 쓰기 작업이므로 별도 트랜잭션
    @Async("domainEventExecutor")
    @Transactional
    public void increaseViewCount(Long postId) {
        postRepository.findById(postId).ifPresent(post -> {
            post.increaseViewCount();
            postRepository.save(post);
            log.info("[Community] 조회수 증가 완료 | postId={}", postId);
        });
    }

    // 작성자 검증
    // 본인 게시글/댓글만 수정/삭제 가능
    private void validateAuthor(Long ownerId, Long userId) {
        if (!ownerId.equals(userId)) {
            throw new CommunityAccessDeniedException("본인만 수정/삭제할 수 있습니다.");
        }
    }

    // 이미지 개수 검증
    // IMAGE 타입 최대 5개
    private void validateImageCount(List<PostContentCommand> contents) {
        long imageCount = contents.stream()
                .filter(cmd -> "IMAGE".equals(cmd.type()))
                .count();
        if (imageCount > 5) {
            throw new DomainRuleViolationException("이미지는 최대 5장까지 업로드 가능합니다.");
        }
    }

    // TEXT / IMAGE 타입 null 검증
    private void validateContents(List<PostContentCommand> contents) {
        for (PostContentCommand cmd : contents) {
            if ("TEXT".equals(cmd.type()) && (cmd.content() == null || cmd.content().isBlank())) {
                throw new DomainRuleViolationException("TEXT 타입은 content 가 필수입니다.");
            }
            if ("IMAGE".equals(cmd.type()) && (cmd.imageUrl() == null || cmd.imageUrl().isBlank())) {
                throw new DomainRuleViolationException("IMAGE 타입은 imageUrl 이 필수입니다.");
            }
        }
    }

    // 이미지 업로드
    // S3 업로드 후 CloudFront URL 반환
    @Override
    public String uploadImage(MultipartFile image) {
        try {
            String key = communityMetrics.getImageUploadTimer().record(
                    () -> s3UploadPort.upload(image, "community/images")
            );
            String url = cloudFrontUrlConverter.convert(key);
            log.info("[Community] 이미지 업로드 완료 | url={}", url);
            return url;
        } catch (Exception e) {
            // 업로드 실패 횟수 카운트
            communityMetrics.recordImageUploadFailed();
            log.error("[Community] 이미지 업로드 실패 | 예외={}", e.getMessage());
            throw e;
        }
        }
}
