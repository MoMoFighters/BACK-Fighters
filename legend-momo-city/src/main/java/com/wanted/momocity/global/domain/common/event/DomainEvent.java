package com.wanted.momocity.global.domain.common.event;

import java.time.Instant;

/*
 * DomainEvent는 EventStorming에서 말하는 "이미 발생한 중요한 비즈니스 사실"을
 * 코드에서 공통으로 다루기 위한 타입이다.
 *
 * Command와 DomainEvent의 차이:
 * - Command:     "수강 신청해 주세요"        — 앞으로 수행할 요청
 * - DomainEvent: "수강이 시작되었다"          — 이미 발생한 결과
 *
 * momocity 예시:
 * - "결제가 완료되었다"           → PaymentCompletedEvent
 * - "강의 영상 인코딩이 끝났다"   → VideoEncodingFinishedEvent
 * - "댓글이 등록되었다"           → CommentCreatedEvent
 * - "수강이 시작되었다"           → EnrollmentStartedEvent
 *
 * 왜 공통 인터페이스가 필요한가?
 * - Aggregate가 여러 종류의 이벤트를 같은 방식으로 보관할 수 있다.
 * - Application Service가 이벤트 구체 타입을 몰라도 publishAll() 로 발행할 수 있다.
 * - "도메인 이벤트는 발생 시각을 가진 비즈니스 사실" 이라는 정의를 코드로 강제할 수 있다.
 *
 * 주의:
 * 모든 상태 변경을 DomainEvent로 만들 필요는 없다.
 * 다른 컨텍스트(알림, 통계, 검색 색인, 스트릭 갱신 등) 후속 작업이 반응할 만큼
 * 의미 있는 비즈니스 결과만 DomainEvent로 둔다.
 */
public interface DomainEvent {

    /*
     * occurredAt 은 이벤트가 "언제 발생했는가" 를 나타낸다.
     * 이벤트를 처리하는 쪽에서는 이 값을 기준으로
     * 로그, 알림 발송 시점, 통계 적재 시점을 해석한다.
     */
    Instant occurredAt();
}
