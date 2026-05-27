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
        1. 이 record 의 역할 : 대시보드 요약 데이터 묶음 (회원/신고/강의 총 개수)
        2. 필드 3개 의미 : memberCount(회원 총 수), reportCount(신고 총 수), lectureCount(강의 총 수)
        3. 왜 long 타입인가 : 카운트는 음수가 될 수 없기 때문에 수십억 가능성 대비 int 대신 long
     */
    record DashboardSummary(
            long memberCount,
            long reportCount,
            long lectureCount
    ) { }
}