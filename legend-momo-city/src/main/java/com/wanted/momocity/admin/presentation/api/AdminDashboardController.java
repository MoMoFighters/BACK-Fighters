package com.wanted.momocity.admin.presentation.api;

import com.wanted.momocity.admin.application.usecase.AdminDashboardQueryUseCase;
import com.wanted.momocity.admin.application.usecase.AdminDashboardQueryUseCase.DashboardSummary;
import com.wanted.momocity.admin.presentation.api.response.DashboardSummaryResponse;
import com.wanted.momocity.global.presentation.api.common.ApiResponse;
import com.wanted.momocity.global.presentation.api.common.ApiResponseCode;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/* comment.
    AdminDashboardController 정리
    DashboardSummary 를 DashboardSummaryResponse 변환 로직만 교체한다.
    나머지 구조는 그대로 유지된다.
 */
@RestController
@RequestMapping("/api/v1")
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "Admin - 대시보드", description = "관리자 대시보드 통계 (회원/신고/강의 수)")
@RequiredArgsConstructor
public class AdminDashboardController {

    private final AdminDashboardQueryUseCase dashboardQueryUseCase;


    @GetMapping("/dashboard/summary")
    @Operation(
            summary = "관리자 대시보드 요약 통계",
            description = "회원 / 신고 / 강의 총 개수를 한 번에 조회한다. FE 대시보드 페이지 진입 시 호출."
    )
    public ResponseEntity<ApiResponse<DashboardSummaryResponse>> getDashboardSummary() {
        // 1. UseCase 호출 → 응용 출력 획득
        DashboardSummary summary = dashboardQueryUseCase.getDashboardSummary();

        // 2. 응용 출력 → 응답 DTO 변환
        DashboardSummaryResponse response = new DashboardSummaryResponse(

                        // Cards : 수치 4개를 관리자 메인 페이지 반환
                        new DashboardSummaryResponse.Cards(
                                summary.cards().totalUsers(),
                                summary.cards().unresolvedReports(),
                                summary.cards().pendingTeachers(),
                                summary.cards().activeLectures()
                        ),

                        // SystemHealth : 인프라 상태 변환
                        new DashboardSummaryResponse.SystemHealth(
                                summary.systemHealth().webService(),
                                summary.systemHealth().database(),
                                summary.systemHealth().fileStorage(),
                                summary.systemHealth().mailService()
                        ),

                        // pendingTasks : 대기 작업 목록 변환
                        summary.pendingTasks().stream()
                                .map(t -> new DashboardSummaryResponse.PendingTask(
                                        t.type(), t.title(), t.requester(), t.requestedAt()))
                                .toList(),

                        // recentReports : 최근 신고 목록 변환
                        summary.recentReports().stream()
                                .map(r -> new DashboardSummaryResponse.RecentReport(
                                        r.reportId(), r.reporterName(), r.reason(), r.isResolved(), r.createdAt()))
                                .toList(),

                        // recentNotices : 최근 공지 목록 변환
                        summary.recentNotices().stream()
                                .map(n -> new DashboardSummaryResponse.RecentNotice(
                                        n.noticeId(), n.title(), n.createdAt()))
                                .toList(),

                        // recentAccessLogs : 최근 접근 로그 목록 변환
                        summary.recentAccessLogs().stream()
                                .map(l -> new DashboardSummaryResponse.RecentAccessLog(
                                        l.logId(), l.ip(), l.userName(), l.role(), l.isSuccess(), l.accessedAt()))
                                .toList()
                );

        // 3. 공통 응답 엔벨로프로 감싸 200 OK 반환
        return ResponseEntity.ok(
                ApiResponse.success(
                        ApiResponseCode.SUCCESS,
                        "대시보드 요약 통계 조회 성공",
                        response
                )
        );
    }
}