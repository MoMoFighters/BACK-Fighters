package com.wanted.momocity.admin.presentation.api;

import com.wanted.momocity.admin.application.usecase.AdminDashboardQueryUseCase;
import com.wanted.momocity.admin.presentation.api.response.DashboardSummaryResponse;
import com.wanted.momocity.global.presentation.api.common.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/* comment.
    AdminDashboardController 정리
    1. 이 클래스의 역할 : 관리자 대시보드 통계 HTTP API 진입점. FE 대시보드 페이지 열 때 호출
    2. 다루는 API :
        - GET /api/v1/admin/dashboard/summary
    3. 클래스 레벨 어노테이션 :
        - @RestController              : REST API 컨트롤러, 반환값 자동 JSON 변환
        - @RequestMapping("/api/v1/admin") : 클래스 안 모든 핸들러 URL 앞에 공통 prefix
        - @PreAuthorize("hasRole('ADMIN')") : 모든 핸들러 호출 전 권한 검사. ADMIN 아니면 403
        - @Tag                         : Swagger UI 에서 "Admin - 대시보드" 그룹으로 표시
    4. 의존성 :
        - AdminDashboardQueryUseCase : UseCase 인터페이스 의존
    5. MS-6 컨트롤러와 핵심 차이 :
        - HTTP 메서드 : GET 방식과 PATCH 방식
 */
@RestController
@RequestMapping("/api/v1/admin")
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "Admin - 대시보드", description = "관리자 대시보드 통계 (회원/신고/강의 수)")
public class AdminDashboardController {

    private final AdminDashboardQueryUseCase dashboardQueryUseCase;

    public AdminDashboardController(AdminDashboardQueryUseCase dashboardQueryUseCase) {
        this.dashboardQueryUseCase = dashboardQueryUseCase;
    }

    /* comment.
        실제 구현 시 흐름 (m03 우선순위) :
        1. DashboardSummary summary = dashboardQueryUseCase.getDashboardSummary();
        *
        2. DashboardSummaryResponse response = new DashboardSummaryResponse(
            summary.memberCount(),
            summary.memberGrowthRate(),
            summary.lectureCount(),
            summary.lectureGrowthRate(),
            summary.reportCount()
        );
        *
        3. return ResponseEntity.ok(response);
  */
    @GetMapping("/dashboard/summary")
    @Operation(
            summary = "관리자 대시보드 요약 통계",
            description = "회원 / 신고 / 강의 총 개수를 한 번에 조회한다. FE 대시보드 페이지 진입 시 호출."
    )
    public ResponseEntity<ApiResponse<DashboardSummaryResponse>> getDashboardSummary() {
        throw new UnsupportedOperationException("TODO: m03 우선순위 - admin 대시보드 요약 통계 컨트롤러 구현");
    }
}