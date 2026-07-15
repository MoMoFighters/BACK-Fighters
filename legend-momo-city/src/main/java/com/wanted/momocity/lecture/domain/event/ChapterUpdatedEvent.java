package com.wanted.momocity.lecture.domain.event;

import com.wanted.momocity.global.domain.common.event.DomainEvent;

import java.time.Instant;

// 챕터 수정 트랜잭션이 완료 된 후 Redis 캐시를 무효화 하기 위한 이벤트
public record ChapterUpdatedEvent(
        Long chapterId,
        Long lectureId,
        Instant occurredAt
) implements DomainEvent {

}
