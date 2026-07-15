package com.wanted.momocity.viewing.infrastructure.event;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;
import com.wanted.momocity.lecture.domain.event.LectureDeletedEvent;

@Slf4j
@Component
public class LectureCacheInvalidationEventHandler {

    // 강의 삭제 트랜잭션이 정상 커밋된 후에만 실행
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    // 강의 단건, 챕터 목록, 챕터 단건 캐시를 함께 삭제
    @Caching(evict = {
            // lecture :: {lectureId} 강의 단건 캐시를 삭제
            @CacheEvict(
                    value = "lecture",
                    key = "#event.lectureId()",
                    cacheManager = "redisCacheManager"
            ),
            // chapter :: {chapterId} 챕터 단건 캐시 삭제
            @CacheEvict(
                    value = "chapters",
                    key = "#event.lectureId()",
                    cacheManager = "redisCacheManager"
            ),
            // 삭제된 강의의 챕터 ID 목록이 이벤트에 없으므로 탭터 단건 캐시 전체 삭제
            @CacheEvict(
                    value = "chapter",
                    allEntries = true,
                    cacheManager = "redisCacheManager"
            )
    })
    public void handleLectureDeleted (LectureDeletedEvent event) {
        log.info("[Viewing] 강의 삭제 캐시 제거 - lectureId={}",
                event.lectureId());
    }
}
