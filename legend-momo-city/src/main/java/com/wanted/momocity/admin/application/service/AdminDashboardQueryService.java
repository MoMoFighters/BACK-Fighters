package com.wanted.momocity.admin.application.service;

import com.wanted.momocity.admin.application.usecase.AdminDashboardQueryUseCase;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/* comment.
    AdminDashboardQueryService 정리
    1. 이 클래스의 역할 : AdminDashboardQueryUseCase 의 구현체 / 다른 영역 통계를 모은다.
    2. 위치 : admin/application/service (응용 계층 - 구현)
    3. 왜 @Transactional 에 readOnly = true 인가 (MS-6 와 다른 부분!) : Query 라서 조회만 하게 진행함
    4. 왜 의존성 필드가 아직 비어있는가 : 다른 영역의 공개 서비스 단계라 주입 보류
    5. MS-6 의 MemberCommandService 와 핵심 차이 : MemberCommandService 는 자기 영역 데이터만 변경
 */
@Service
@Transactional(readOnly = true)
public class AdminDashboardQueryService implements AdminDashboardQueryUseCase {

    /* comment.
        m03 우선순위에서 추가될 의존성 :
        - private final MemberStatsPort memberStatsPort;   (외부 BC 회원 - PORT 패턴, 어댑터는 회원팀 제공)
        - private final LectureStatsPort lectureStatsPort; (외부 BC 강의 - PORT 패턴, 어댑터는 성진 제공)
        - private final ReportStatsPort reportStatsPort;   (외부 BC 신고 - PORT 패턴, 어댑터는 report BC 가 제공)
        *
        BC 정합 - 3개 모두 외부 BC 라 PORT 패턴 일관 적용 :
        - admin BC 는 다른 BC 의 내부 구현을 모름 (BC 격리)
        - 각 Port 의 어댑터는 데이터 소유자 BC 가 제공 (member / lecture / report 모두 동일)
        *
        현재는 회원/강의 어댑터 미구현 상태라 주입 보류.
        ReportStatsPort 는 어댑터(ReportStatsAdapter) 완성됨 → 즉시 주입 가능.
        모두 준비되면 생성자 주입 + 메서드 본문 실구현 예정.
 */
    public AdminDashboardQueryService() {
        // m03 우선순위 - 위 3개 Port 생성자 주입 예정
    }

    /* comment.
        실제 구현 시 흐름 (m03 우선순위) :
        *
        // 전월 말 시점 계산 (예: 5월 27일 호출 시 → 4월 30일)
        LocalDate previousMonthEnd = LocalDate.now().withDayOfMonth(1).minusDays(1);
        *
        // 회원 카운트 + 증감률
        long memberCount        = memberStatsPort.countActive();
        long memberPrevious     = memberStatsPort.countActiveBefore(previousMonthEnd);
        double memberGrowthRate = calcGrowthRate(memberPrevious, memberCount);
        *
        // 강의 카운트 + 증감률
        long lectureCount        = lectureStatsPort.countActive();
        long lecturePrevious     = lectureStatsPort.countActiveBefore(previousMonthEnd);
        double lectureGrowthRate = calcGrowthRate(lecturePrevious, lectureCount);
        *
        // 신고 카운트 (증감률 없음 - FE 표시 안 함)
        long reportCount = reportQueryUseCase.countAll();
        *
        return new DashboardSummary(
            memberCount, memberGrowthRate,
            lectureCount, lectureGrowthRate,
            reportCount
        );
    */
    // 헬퍼 메서드 (private double)
    // calcGrowthRate(long previous, long current):
    //   if (previous == 0) return 0.0;
    //   return ((current - previous) / (double) previous) * 100.0;

    @Override
    public DashboardSummary getDashboardSummary() {
        throw new UnsupportedOperationException("TODO: m03 우선순위 - admin 대시보드 요약 통계 구현");
    }
}