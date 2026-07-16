package com.wanted.momocity.viewing.infrastructure.event;

import com.wanted.momocity.lecture.domain.event.ChapterUpdatedEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;
import com.wanted.momocity.lecture.domain.event.ChapterDeletedEvent;

// 챕터 수정, 삭제 Viewing Redis 캐시를 삭제하는 이벤트 리스너
@Slf4j
@Component
public class ChapterCacheInvalidationEventHandler {

    // DB의 챕터 수정 트랜잭션이 정상 커밋된 후 실행
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    // 단건 캐시와 목록 캐시는 키 값이 다르므로 두 CacheEvict를 함께 적용
    @Caching(evict = {
            // chapter::{chapterId} 단건 캐시를 삭제
            @CacheEvict(
                    value = "chapter",
                    key = "#event.chapterId()",
                    cacheManager = "redisCacheManager"
            ),
            // chapters::{lectureId} 목록 캐시를 삭제합
            @CacheEvict(
                    value = "chapters",
                    key = "#event.lectureId()",
                    cacheManager = "redisCacheManager"
            )
    })
    public void handleChapterUpdated(ChapterUpdatedEvent event) {
        // 캐시 삭제 이벤트가 실행됐는지 확인하기 위한 로그
        log.info(
                "[Viewing] 챕터 수정 캐시 삭제 - lectureId={}, chapterId={}",
                event.lectureId(),
                event.chapterId()
        );
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Caching(evict = {
            @CacheEvict(
                    value = "chapter",
                    key = "#event.chapterId()",
                    cacheManager = "redisCacheManager"
            ),
            @CacheEvict(
                    value = "chapters",
                    key = "#event.lectureId()",
                    cacheManager = "redisCacheManager"
            )
    })
    public void handleChapterDeleted(ChapterDeletedEvent event) {
        log.info(
                "[Viewing] 챕터 삭제 캐시 제거 - lectureId={}, chapterId={}",
                event.lectureId(),
                event.chapterId()
        );
    }
}