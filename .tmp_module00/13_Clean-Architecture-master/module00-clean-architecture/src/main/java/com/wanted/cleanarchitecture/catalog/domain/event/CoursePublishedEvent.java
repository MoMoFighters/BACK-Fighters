package com.wanted.cleanarchitecture.catalog.domain.event;

import com.wanted.cleanarchitecture.global.domain.common.event.DomainEvent;

import java.time.Instant;

/*
 * CoursePublishedEvent는 "강의가 공개되었다"는 도메인 결과다.
 *
 * Command와의 차이:
 * - PublishCourseCommand: 강의를 공개해 달라는 요청
 * - CoursePublishedEvent: 강의가 실제로 공개되었다는 결과
 *
 * Instant를 사용하는 이유:
 * - Domain Event는 "언제 발생했는가"를 남겨야 한다.
 * - LocalDateTime은 시간대 정보가 없어 서버/지역이 달라지면 해석이 달라질 수 있다.
 * - Instant는 UTC 기준의 한 시점을 표현하므로 로그, 이벤트, 통계, 외부 시스템 연동에 적합하다.
 *
 * 수업용 정리:
 * Domain Event에는 후속 처리에 필요한 최소 정보만 담는다.
 * 여기서는 어떤 강의가 언제 공개되었는지만 필요하므로 courseId와 occurredAt만 둔다.
 */
public record CoursePublishedEvent(
        Long courseId,
        Instant occurredAt
) implements DomainEvent {
}
