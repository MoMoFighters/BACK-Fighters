package com.wanted.momocity.lecture.domain.event;

import com.wanted.momocity.global.domain.common.event.DomainEvent;

import java.time.Instant;

// 강의 트렌젝션이 완료 후 후속 작업을 요청하는 이벤트
public record LectureDeletedEvent(
        Long lectureId,
        Instant occurredAt
) implements DomainEvent {
}
