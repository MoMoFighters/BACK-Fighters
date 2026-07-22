package com.wanted.momocity.community.application.post.service;

import com.wanted.momocity.community.application.post.port.ThumbnailPort;
import com.wanted.momocity.community.infrastructure.persistence.PostJpaRepository;
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
    private final PostJpaRepository postJpaRepository;
    private final org.springframework.cache.CacheManager cacheManager;

    @Async("domainEventExecutor")
    public void resizeThumbnailAsync(Long postId, String originalThumbnailUrl) {

        // 1. 리사이징 시도 (DB 커넥션 없이 순수 외부 I/O)
        String resizedUrl = thumbnailPort.generateThumbnail(originalThumbnailUrl);

        // 2. 리사이징 실패 시(원본 URL 그대로 반환) 스킵 — DB/캐시 아무것도 건드리지 않음
        if (resizedUrl.equals(originalThumbnailUrl)) {
            log.warn("[Thumbnail] 리사이징 결과가 원본과 동일, 갱신 스킵 | postId={}", postId);
            return;
        }

        // 3. 조건부 UPDATE
        //    - originalThumbnailUrl 이 현재 DB 값과 같을 때만 UPDATE 수행
        //    - 그 사이 사용자가 썸네일을 바꿨으면 0 반환 → 덮어쓰기 방지
        int updatedRows = updateThumbnail(postId, originalThumbnailUrl, resizedUrl);

        if (updatedRows == 0) {
            log.info("[Thumbnail] 이미 다른 썸네일로 교체됨, 갱신 스킵 | postId={}", postId);
            return;
        }

        log.info("[Thumbnail] DB 갱신 완료 | postId={}, resizedUrl={}", postId, resizedUrl);

        // 4. UPDATE 성공한 경우에만 캐시 무효화
        Cache postsCache = cacheManager.getCache("posts");
        if (postsCache != null) {
            postsCache.clear();
            log.info("[Thumbnail] posts 캐시 무효화 완료 | postId={}", postId);
        }
    }

    /*
     * comment.
     *  @Transactional 을 별도 메서드로 분리
     *  - generateThumbnail() 의 외부 I/O 구간은 트랜잭션 밖에서 실행되고
     *    실제 DB 쓰기(UPDATE)만 트랜잭션 안에서 수행
     */

    protected int updateThumbnail(Long postId, String currentThumbnailUrl, String newThumbnailUrl) {
        return postJpaRepository.updateThumbnailIfUnchanged(postId, currentThumbnailUrl, newThumbnailUrl);
    }

}
