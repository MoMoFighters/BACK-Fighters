package com.wanted.momocity.lecture.domain.event;

import com.wanted.momocity.global.domain.common.event.DomainEvent;
import com.wanted.momocity.lecture.domain.model.LectureStatus;

import java.time.Instant;

// 관리자가 강의 강태를 승인 또는 거절로 변경했을 때 발행하는 이벤트
public record LectureStatusChangedEvent(
        Long lectureId,
        Long teacherId,
        Long adminId,
        String lectureTitle,
        LectureStatus lectureStatus,
        // 이벤트 발행 시간
        Instant occurredAt
) implements DomainEvent {
}
