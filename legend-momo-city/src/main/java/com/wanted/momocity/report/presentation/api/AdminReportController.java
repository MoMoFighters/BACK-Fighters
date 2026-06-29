package com.wanted.momocity.report.presentation.api;

import com.wanted.momocity.global.presentation.api.common.ApiResponse;
import com.wanted.momocity.global.presentation.api.common.ApiResponseCode;
import com.wanted.momocity.report.application.usecase.ReportCommandUseCase;
import com.wanted.momocity.report.application.usecase.ReportQueryUseCase;
import com.wanted.momocity.report.presentation.api.response.ReportDetailResponse;
import com.wanted.momocity.report.presentation.api.response.ReportListResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/* comment.
    AdminReportController 정리
    관리자 전용 신고 API 로 ADMIN 권한이 필수적이다.
    MS-1 신고 목록 / MS-2 신고 상세 / MS-3 신고 처리 담당의 내용이 들어있다.
    Query(조회) -> ReportQueryUseCase, Command(처리) -> ReportCommandUseCase
    ReportController(회원 신고 접수)와 base path 공유, 권한과 역할로 분리했다.
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/reports")
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "Admin - 신고", description = "관리자 신고 목록 조회")
public class AdminReportController {

    private final ReportQueryUseCase reportQueryUseCase;
    private final ReportCommandUseCase reportCommandUseCase;

    @GetMapping
    @Operation(
            summary = "관리자 신고 목록 조회",
            description = "최근 N개의 신고를 조회한다. isResolved 파라미터로 처리 여부 필터링 가능"
    )
    // Swagger 구성하기 위한 코드
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200", description = "신고 목록 조회 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "401", description = "인증 토큰 누락 또는 만료"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "403", description = "ADMIN 권한 없음")
    })
    public ResponseEntity<ApiResponse<ReportListResponse>> getReports(
            @Parameter(description = "조회할 최대 개수", example = "10")
            @RequestParam(defaultValue = "10") int limit,
            @Parameter(description = "처리 여부 필터 (선택, false=미처리, true=처리완료)", example = "false")
            @RequestParam(required = false) Boolean isResolved
    ) {
        // 1. isResolved 유무에 따라 UseCase 메서드 선택
        ReportQueryUseCase.ReportList list = (isResolved == null)
                ? reportQueryUseCase.getRecent(limit)
                : reportQueryUseCase.getByIsResolved(isResolved, limit);

        // 2. UseCase 출력 → 응답 DTO 변환
        ReportListResponse response = ReportListResponse.from(list);

        // 3. 200 OK + 공통 응답 wrapper 로 반환
        return ResponseEntity.ok(
                ApiResponse.success(
                        ApiResponseCode.SUCCESS,
                        "신고 목록 조회 성공",
                        response
                )
        );
    }

    // MS-2 신고 상세 조회
    @GetMapping("/{id}")
    @Operation(summary = "신고 상세 조회", description = "신고 1건의 전체 정보를 조회한다.")
    public ResponseEntity<ApiResponse<ReportDetailResponse>> getReport(@PathVariable Long id) {
        // 1. UseCase 호출 → ReportDetail 획득
        ReportQueryUseCase.ReportDetail detail = reportQueryUseCase.getById(id);

        // 2. 도메인 → 응답 DTO 변환
        ReportDetailResponse response = ReportDetailResponse.from(detail);

        // 3. 200 OK 반환
        return ResponseEntity.ok(
                ApiResponse.success(ApiResponseCode.SUCCESS, "신고 상세 조회 성공", response)
        );
    }

    // MS-3 신고 처리
    @PatchMapping("/{id}/resolve")
    @Operation(summary = "신고 처리", description = "신고를 처리 완료 상태로 변경한다. isResolved=true, resolvedAt 기록.")
    public ResponseEntity<ApiResponse<Void>> resolveReport(
            @PathVariable Long id
    ) {
        // 이게 없다면 신고 처리 아무것도 안하고 200만 반환하는 버그 발생
        reportCommandUseCase.resolveReport(id);

        // 1. 200 OK 반환 (data=null)
        return ResponseEntity.ok(
                ApiResponse.success(ApiResponseCode.SUCCESS, "신고 처리 완료", null)
        );
    }
}