package com.wanted.momocity.viewing.infrastructure.event;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Caching;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;
import com.wanted.momocity.lecture.domain.event.LectureDeletedEvent;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class LectureCacheInvalidationEventHandler {

    private final StringRedisTemplate stringRedisTemplate;

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
            )
    })
    public void handleLectureDeleted (LectureDeletedEvent event) {

        // 이벤트의 챕터 ID들을 실제 Redis 단건 캐시 키 형식으로 반환
        List<String> chapterCacheKeys = event.chapterIds()
                        .stream()
                        .map(chapterId -> "chapter::" + chapterId)
                        .toList();

        // 삭제할 챕터 단건 캐시가 있을 때만 Redis 삭제 실행
        if (!chapterCacheKeys.isEmpty()) {
            try{
                // 관련 챕터 단건 캐시 키들만 Redis 삭제 실행
                Long deletedCount = stringRedisTemplate.delete(chapterCacheKeys);
                log.info(
                        "[Viewing] 강의 소속 챕터 단건 캐시 제거 - lectureId={}, requestedCount={}, deletedCount={}",
                        event.lectureId(),
                        chapterCacheKeys.size(),
                        deletedCount
                );
            } catch (Exception exception) {
                log.warn(
                        "[Viewing] 강의 소속 챕터 단건 캐시 제거 실패 - lectureId={}",
                        event.lectureId(),
                        exception
                );
            }
        }
        log.info("[Viewing] 강의 삭제 캐시 제거 - lectureId={}",
                event.lectureId());
    }
}
