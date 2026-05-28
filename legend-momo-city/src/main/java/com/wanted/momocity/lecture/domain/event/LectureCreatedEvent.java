package com.wanted.momocity.lecture.domain.event;

import com.wanted.momocity.global.domain.common.event.DomainEvent;

import java.time.Instant;

// 강의 등록이 완료되었을 때 발행하는 이벤트
// 이후 관리자 알림, 승인 대기 처리, 로그 기록 같은 후속 작업에서 사용 가능
public record LectureCreatedEvent(
        Long lectureId,
        Long teacherId,
        String title,
        Instant occurredAt
) implements DomainEvent {
}