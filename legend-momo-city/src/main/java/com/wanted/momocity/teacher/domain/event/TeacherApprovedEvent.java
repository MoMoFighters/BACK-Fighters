package com.wanted.momocity.teacher.domain.event;

import com.wanted.momocity.global.domain.common.event.DomainEvent;

import java.time.Instant;

/*
 * TeacherApprovedEvent 는 "강사 신청이 승인되었다"는 도메인 결과를 나타낸다.
 *
 * 후속 처리:
 *  - 알림 영역(notification BC)이 이 이벤트를 수신해 강사에게 승인 알림을 발송.
 *    (m03에서는 수신부 stub 만 존재, 실제 발송은 m04+)
 *
 * REF: module00-clean-architecture catalog/domain/event/CoursePublishedEvent.java
 */
public record TeacherApprovedEvent(
        Long userId,
        Instant occurredAt
) implements DomainEvent {
}
