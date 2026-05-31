package com.wanted.momocity.admin.application.service;

import com.wanted.momocity.admin.application.port.LectureStatsPort;
import com.wanted.momocity.admin.application.port.MemberStatsPort;
import com.wanted.momocity.admin.application.port.ReportStatsPort;
import com.wanted.momocity.admin.application.usecase.AdminDashboardQueryUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

/* comment.
    AdminDashboardQueryService 정리
    1. 이 클래스의 역할 : AdminDashboardQueryUseCase 의 구현체 / 다른 영역 통계를 모은다.
    2. 위치 : admin/application/service (응용 계층 - 구현)
    3. 왜 @Transactional 에 readOnly = true 인가 (MS-6 와 다른 부분!) : Query 라서 조회만 하게 진행함
    4. 의존성 : 외부 BC 통계 Port 3개 (Member/Lecture/Report) 주입 — 구현 아닌 인터페이스에 의존
    5. MS-6 의 MemberCommandService 와 핵심 차이 : MemberCommandService 는 자기 영역 데이터만 변경
 */
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class AdminDashboardQueryService implements AdminDashboardQueryUseCase {

    /* comment.
        의존성 - 외부 BC 통계 Port 3개 (모두 PORT 패턴 일관 적용)
        1. admin BC 는 다른 BC 의 내부 구현을 모르고 인터페이스(Port)에만 의존한다 (BC 격리 / DIP).
        2. 각 Port 의 어댑터는 "데이터 소유자 BC" 가 제공한다.
           - memberStatsPort  → MemberStatsAdapter  (user BC 제공)
           - lectureStatsPort → LectureStatsAdapter (lecture BC 제공)
           - reportStatsPort  → ReportStatsAdapter  (report BC 제공)
        3. @RequiredArgsConstructor 가 final 필드 3개를 받는 생성자를 자동 생성 → 스프링이 어댑터 빈을 주입.
     */
    private final MemberStatsPort memberStatsPort;
    private final LectureStatsPort lectureStatsPort;
    private final ReportStatsPort reportStatsPort;

    /* comment.
        getDashboardSummary - 여러 BC 의 통계를 모아 대시보드 요약으로 조합한다.
        처리 흐름 5단계 :
        a) 증감률 기준 시점(전월 말일) 계산
        b) 회원   : 현재 수 + 전월 말 수 → 증감률
        c) 강의   : 현재 수 + 전월 말 수 → 증감률
        d) 신고   : 전체 수 (증감률 없음 - FE 표시 안 함)
        e) DashboardSummary 로 묶어 반환
     */
    @Override
    public DashboardSummary getDashboardSummary() {
        // a) 전월 말 시점 (예: 5/30 호출 → 이번달 1일에서 하루 빼면 4/30)
        LocalDate previousMonthEnd = LocalDate.now().withDayOfMonth(1).minusDays(1);

        // b) 회원 카운트 + 증감률
        long memberCount        = memberStatsPort.countActive();
        long memberPrevious     = memberStatsPort.countActiveBefore(previousMonthEnd);
        double memberGrowthRate = calcGrowthRate(memberPrevious, memberCount);

        // c) 강의 카운트 + 증감률
        long lectureCount        = lectureStatsPort.countActive();
        long lecturePrevious     = lectureStatsPort.countActiveBefore(previousMonthEnd);
        double lectureGrowthRate = calcGrowthRate(lecturePrevious, lectureCount);

        // d) 신고 카운트 (전체 신고 수만 - 증감률 없음)
        long reportCount = reportStatsPort.countAll();

        // e) 요약 결과로 묶어 반환
        return new DashboardSummary(
                memberCount, memberGrowthRate,
                lectureCount, lectureGrowthRate,
                reportCount
        );
    }

    /* comment.
        calcGrowthRate - 전월 대비 증감률(%) 계산
        - previous(이전 시점 수) 가 0 이면 0 으로 나눌 수 없으므로 0.0 반환 (방어 로직)
        - 공식 : (현재 - 이전) / 이전 * 100  (예: 80 → 100 이면 +25.0)
        - (double) 캐스팅으로 정수 나눗셈이 아닌 실수 나눗셈 보장
     */
    private double calcGrowthRate(long previous, long current) {
        if (previous == 0) {
            return 0.0;
        }
        return ((current - previous) / (double) previous) * 100.0;
    }
}