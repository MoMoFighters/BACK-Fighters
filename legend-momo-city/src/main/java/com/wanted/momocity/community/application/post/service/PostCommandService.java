package com.wanted.momocity.community.application.post.service;

import com.wanted.momocity.community.application.post.command.PostContentCommand;
import com.wanted.momocity.community.application.post.result.PostCreateResult;
import com.wanted.momocity.community.application.post.usecase.PostCommandUseCase;
import com.wanted.momocity.community.domain.event.ThumbnailResizeRequestedEvent;
import com.wanted.momocity.community.domain.exception.CommunityAccessDeniedException;
import com.wanted.momocity.community.domain.exception.CommunityNotFoundException;
import com.wanted.momocity.community.domain.model.*;
import com.wanted.momocity.community.domain.repository.PostContentRepository;
import com.wanted.momocity.community.domain.repository.PostRepository;
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
* */

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class PostCommandService implements PostCommandUseCase {

    private final PostRepository postRepository;
    private final PostContentRepository postContentRepository;
    private final CloudFrontUrlConverter cloudFrontUrlConverter;
    private final S3UploadPort s3UploadPort;
    private final ApplicationEventPublisher eventPublisher;
    // 카테고리별 기본 썸네일
    // thumbnailUrl 미지정 + 이미지 없는 게시글에 한해 카테고리 기준 기본 썸네일 자동 생성
    private static final String DEFAULT_THUMBNAIL_BASE_URL =
            "https://momocity-bucket.s3.ap-northeast-2.amazonaws.com/community/thumbnails/";

    /*
     * comment.
     *  썸네일 결정 로직
     *  1. IMAGE 타입 콘텐츠 존재 + thumbnailUrl 없음
     *     -> 에러 (사용자가 직접 썸네일 지정 필요)
     *  2. IMAGE 타입 콘텐츠 없음 + thumbnailUrl 없음
     *     -> 카테고리 기준 기본 썸네일 자동 설정
     *  3. thumbnailUrl 있음
     *     -> 그대로 사용
     */
    private String resolveThumbnailUrl(
            String thumbnailUrl, List<PostContentCommand> contents, PostCategory category
    ) {
        // thumbnailUrl 이미 지정됨 → 그대로 사용
        // "null" 문자열도 미지정으로 처리 (프론트 직렬화 이슈 방지)
        if (thumbnailUrl != null && !thumbnailUrl.isBlank() && !"null".equalsIgnoreCase(thumbnailUrl)) {
            return thumbnailUrl;
        }

        // IMAGE 타입 콘텐츠 존재 여부 확인
        boolean hasImage = contents.stream()
                .anyMatch(cmd -> "IMAGE".equals(cmd.type()));

        // 이미지 업로드했는데 썸네일 미지정 → 에러
        if (hasImage) {
            throw new DomainRuleViolationException("이미지를 업로드했다면 썸네일을 지정해주세요.");
        }

        // 이미지 없는 게시글 → 카테고리 기준 기본 썸네일 자동 설정
        return DEFAULT_THUMBNAIL_BASE_URL + category.name() + ".png";
    }

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

        log.info("[Community] 게시글 생성 완료 | userId={}, postId={}", userId, saved.getId());
        return new PostCreateResult(saved.getId());
    }

    @Override
    public void uploadContents(Long userId, Long postId, String thumbnailUrl, List<PostContentCommand> contents) {

        // 게시글 조회 (존재 여부 확인)
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new CommunityNotFoundException("게시글을 찾을 수 없습니다."));

        // 게시글 작성자 본인 검증
        validateAuthor(post.getUserId(), userId);
        // 이미지 개수 검증 (최대 이미지 개수 초과 방지)
        validateImageCount(contents);
        // 콘텐츠 타입별 필수값 검증 (TEXT -> content, IMAGE -> imageUrl)
        validateContents(contents);

        // 썸네일 URL 결정 (미지정 시 기본 썸네일 또는 에러)
        String resolvedThumbnailUrl = resolveThumbnailUrl(thumbnailUrl, contents, post.getCategory());

        // 썸네일 업데이트
        post.updateThumbnail(resolvedThumbnailUrl);
        postRepository.save(post);

        // 사용자가 직접 지정한 썸네일일 때만 비동기 리사이징 트리거
        if (!resolvedThumbnailUrl.startsWith(DEFAULT_THUMBNAIL_BASE_URL)) {
            eventPublisher.publishEvent(new ThumbnailResizeRequestedEvent(postId, resolvedThumbnailUrl));
        }

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
        log.info("[Community] 콘텐츠 업로드 완료 | postId={}, count={}", postId, postContents.size());
    }

    // 게시글 제목 / 카테고리 수정
    // 제목 / 카테고리 수정 시 목록 캐시 전체 무효화
    @Override
    @CacheEvict(value = "posts", allEntries = true, cacheManager = "redisCacheManager")
    public void updatePost(Long userId, Long postId, String title, PostCategory category) {

        // 게시글 조회 (존재 여부 확인)
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new CommunityNotFoundException("게시글을 찾을 수 없습니다."));

        // 게시글 작성자 본인 검증
        validateAuthor(post.getUserId(), userId);

        // 제목 / 카테고리 수정
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

        // 썸네일 URL 결정 (미지정 시 기본 썸네일 또는 에러)
        String resolvedThumbnailUrl = resolveThumbnailUrl(thumbnailUrl, contents, post.getCategory());

        // 썸네일 업데이트
        post.updateThumbnail(resolvedThumbnailUrl);
        postRepository.save(post);

        // 사용자가 직접 지정한 썸네일일 때만 비동기 리사이징  트리거
        if (!resolvedThumbnailUrl.startsWith(DEFAULT_THUMBNAIL_BASE_URL)) {
            eventPublisher.publishEvent(new ThumbnailResizeRequestedEvent(postId, resolvedThumbnailUrl));
        }

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

        // 게시글 존재 여부 확인
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new CommunityNotFoundException("게시글을 찾을 수 없습니다."));

        // 게시글 작성자 본인 검증
        validateAuthor(post.getUserId(), userId);

        // 게시글 소프트 딜리트
        post.delete();
        postRepository.save(post);

        // 컨텐츠 소프트 딜리트 (게시글 삭제 시 컨텐츠도 함께 삭제)
        postContentRepository.deleteAllByPostId(postId);

        log.info("[Community] 게시글 삭제 완료 | postId={}", postId);
    }

    // 조회수 증가
    // @Async : 별도 스레드에서 실행 -> 응답 속도에 영향 없음
    // domainEventExecutor : AsyncConfig 에 등록된 스레드를 재사용
    // @Transactional : 조회수 증가는 쓰기 작업이므로 별도 트랜잭션
    @Async("domainEventExecutor")
    @Transactional
    public void increaseViewCount(Long postId) {
        // 게시글 존재 시에만 조회수 증가 (없으면 skip)
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

        // S3 에 이미지 업로드 후 key 반환
        String key = s3UploadPort.upload(image, "community/images");

        // CloudFront URL 로 변환 후 반환
        String url = cloudFrontUrlConverter.convert(key);

        log.info("[Community] 이미지 업로드 완료 | url={}", url);
        return url;
    }
}
