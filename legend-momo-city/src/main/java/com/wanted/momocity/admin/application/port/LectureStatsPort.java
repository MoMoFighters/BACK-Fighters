package com.wanted.momocity.admin.application.port;

import java.time.LocalDate;

/* comment.
    LectureStatsPort 정리
    1. 이 인터페이스의 역할 : admin 이 강의 통게 데이터를 받기 위해 정의한 포트
    2. 위치 : admin/application/port (MemberStatsPort 와 동일)
    3. MemberStatsPort 와의 차이 : 통계 대상이 회원이 아닌 강의. 외부 BC 는 Lecture 담당
    4. 누가 어댑터를 제공하는가 : lecture/ 영역에서 어댑터 구현 예정
    5. 현재 어댑터가 없는데 동작에 문제 없는가 : 어댑터가 없으면 빈 등록 단계에서 예외 발생 가능성 존재
    -> 따라서 @ConditionalOnBean 으로 우회 예정
 */
public interface LectureStatsPort {


    // 현재 활성(공개 상태) 강의 수
    long countActive();

     // 특정 날짜 이전 시점의 활성 강의 수 (증감률 계산용)
    long countActiveBefore(LocalDate date);
}