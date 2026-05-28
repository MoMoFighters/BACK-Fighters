package com.wanted.cleanarchitecture.global.domain.common.event;

import java.time.Instant;

/*
 * DomainEvent는 EventStorming에서 말하는 "이미 발생한 중요한 비즈니스 사실"을
 * 코드에서 공통으로 다루기 위한 타입이다.
 *
 * Command와의 차이:
 * - Command: "강의를 공개해 주세요"처럼 앞으로 수행할 요청
 * - DomainEvent: "강의가 공개되었다"처럼 이미 발생한 결과
 *
 * 왜 공통 인터페이스가 필요한가?
 * - Aggregate가 여러 종류의 이벤트를 같은 방식으로 보관할 수 있다.
 * - Application Service가 이벤트 구체 타입을 몰라도 publishAll()로 발행할 수 있다.
 * - 수업에서 "도메인 이벤트는 발생 시각을 가진 비즈니스 사실"이라고 일관되게 설명할 수 있다.
 *
 * 주의:
 * 모든 상태 변경을 DomainEvent로 만들 필요는 없다.
 * 다른 컨텍스트, 알림, 통계, 검색 색인 갱신 같은 후속 작업이 반응할 만큼
 * 의미 있는 비즈니스 결과만 DomainEvent로 둔다.
 */
public interface DomainEvent {

    // 이벤트 발생 시간
    Instant occurredAt();

}
