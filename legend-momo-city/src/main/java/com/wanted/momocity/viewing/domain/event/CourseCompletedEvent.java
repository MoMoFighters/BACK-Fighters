package com.wanted.momocity.viewing.domain.event;

import com.wanted.momocity.global.domain.common.event.DomainEvent;

import java.time.Instant;

/*
* comment.
*  totalProgress = 100 도달 시 발행 -> 수강 상태 변경, 강좌 수료 처리
* */

public record CourseCompletedEvent (
        Long userId,
        Long lectureId,
        Instant occurredAt
) implements DomainEvent {
}
