package com.wanted.momocity.admin.application.port;

import java.time.LocalDate;

/* comment.
    MemberStatsPort 정리
    1. 이 인터페이스의 역할 : admin 이 회원 통계 데이터를 받기 위해서 정의한 포트
    2. 위치 : admin/application/port (응용 계층 - 외부 의존성 정의)
    3. 왜 PORT 패턴인가 (헥사고날 아키텍처) : admin 이 필요한 것만 정의한다.
    -> 외부 BC 가 어댑터로 구현을 한다.
    4. 왜 admin/ 안에 두는가 : admin 영역의 외부 의존성은 즉, admin 이 소유하고있다는 뜻과 동일하다.
    5. 왜 구현체가 없는가 (어댑터는 외부 BC 가 제공) : 구현은 외부 바운더리 컨텍스트 회원 담당자가 어댑터로 제공해준다.
 */
public interface MemberStatsPort {


    // 현재 활성(ACTIVE) 회원 수
    long countActive();

    // 특정 날짜 이전 시점의 활성 회원 수 (증감률 계산용)
    // EX) 전월 말 시점의 회원 수

    long countActiveBefore(LocalDate date);
}