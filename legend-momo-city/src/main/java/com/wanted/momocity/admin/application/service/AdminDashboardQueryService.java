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
        - private final MemberQueryService memberQueryService;     (회원 영역 공개 서비스)
        - private final ReportQueryService reportQueryService;     (신고 영역 공개 서비스 - 미구현)
        - private final LectureQueryService lectureQueryService;   (강의 영역 공개 서비스 - 미구현)

        현재는 다른 영역의 공개 서비스가 아직 stub 단계라 주입 보류.
        모든 영역이 준비되면 생성자 주입으로 추가.
     */
    public AdminDashboardQueryService() {
        // m03 우선순위 - 위 3개 서비스 생성자 주입 예정
    }

    /* comment.
        실제 구현 시 흐름 (m03 우선순위) :
        1. long memberCount = memberQueryService.countAll()
        2. long reportCount = reportQueryService.countAll()
        3. long lectureCount = lectureQueryService.countAll()
        4. return new DashboardSummary(memberCount, reportCount, lectureCount)
     */
    @Override
    public DashboardSummary getDashboardSummary() {
        throw new UnsupportedOperationException("TODO: m03 우선순위 - admin 대시보드 요약 통계 구현");
    }
}