package com.wanted.momocity.community.application.post.service;

import com.wanted.momocity.community.application.post.port.ThumbnailPort;
import com.wanted.momocity.community.domain.repository.PostRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.Cache;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

/*
 * comment.
 *  ThumbnailAsyncService
 *  - 게시글 저장 이후 백그라운드에서 썸네일 리사이징 수행
 *  - domainEventExecutor 재사용 (AsyncConfig 에 이미 등록된 Bean)
 *  - 완료되면 post.thumbnailUrl 을 리사이징된 URL로 UPDATE
 *  - 리사이징 완료 즉시 목록 캐시 무효화 -> 다음 조회부터 바로 새 URL 반영
 */

@Slf4j
@Service
@RequiredArgsConstructor
public class ThumbnailAsyncService {

    private final ThumbnailPort thumbnailPort;
    private final PostRepository postRepository;
    private final org.springframework.cache.CacheManager cacheManager;

    @Async("domainEventExecutor")
    public void resizeThumbnailAsync(Long postId, String originalThumbnailUrl) {

        String resizedUrl = thumbnailPort.generateThumbnail(originalThumbnailUrl);

        // 리사이징 실패 시 원본 URL 그대로 반환되므로, 값이 실제로 바뀐 경우만 갱신
        if (!resizedUrl.equals(originalThumbnailUrl)) {
            postRepository.findById(postId).ifPresentOrElse(
                    post -> {
                        post.updateThumbnail(resizedUrl);
                        postRepository.save(post);
                        log.info("[Thumbnail] DB 갱신 완료 | postId={}, resizedUrl={}", postId, resizedUrl);

                        // DB 갱신 성공 시에만 캐시 무효화 (프로그래매틱 방식)
                        // @CacheEvict와 달리 "성공한 경우에만" 명시적으로 제어 가능
                        Cache postsCache = cacheManager.getCache("posts");
                        if (postsCache != null) {
                            postsCache.clear();
                            log.info("[Thumbnail] posts 캐시 무효화 완료 | postId={}", postId);
                        }

                    },
                    () -> log.warn("[Thumbnail] 게시글을 찾을 수 없어 갱신 스킵 | postId={}", postId)
            );
        }
    }

}
