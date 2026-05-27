package com.wanted.momocity.admin.application.usecase;

/* comment.
    AdminDashboardQueryUseCase 정리
    1. 이 인터페이스의 역할 : 관리자 대시보드 요약 통계 조회 응용 계층 계약
    2. 위치 : admin/application/usecase (응용 계층 - 계약)
    3. 왜 Query 전용인가 (Command 없음) : 조회 전용 - 데이터 변경 없음. CQRS 의 Q 측
    4. 왜 admin 영역에 두는가 (cross-cutting) : 여러 영역의 통계를 모음 = 한 영역에 속하지 않음.
    5. 왜 Result 를 중첩 record 로 두는가 : 출력 계약도 같은 위치
 */
public interface AdminDashboardQueryUseCase {

    DashboardSummary getDashboardSummary();

    /* comment.
        DashboardSummary 정리
        1. 이 record 의 역할 : 대시보드 요약 데이터 묶음 (카운트 + 증감률)
        2. 필드 5개 의미 :
            - memberCount         : 현재 회원 총 수
            - memberGrowthRate    : 회원 증감률 % (전월 대비, +12.0 → "+12%")
            - lectureCount        : 현재 강의 총 수
            - lectureGrowthRate   : 강의 증감률 % (전월 대비)
            - reportCount         : 현재 신고 총 수 (증감률 없음 - FE 표시 안 함)
        3. 왜 카운트는 long, 증감률은 double :
            카운트는 음수 불가 + 수십억 대비 long. 증감률은 % 소수점 가능 (+12.5% 등) → double
        4. 왜 매출은 빠졌나 :
            payment 도메인은 module04 로 미룸. 매출 데이터 소스 없어서 module03 에서는 표시 불가
     */
    record DashboardSummary(
            long memberCount,
            double memberGrowthRate,
            long lectureCount,
            double lectureGrowthRate,
            long reportCount
    ) { }
}