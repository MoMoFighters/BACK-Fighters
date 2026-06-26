package com.wanted.momocity.report.presentation.api;

import com.wanted.momocity.auth.infrastructure.security.CustomUserDetails;
import com.wanted.momocity.global.presentation.api.common.ApiResponse;
import com.wanted.momocity.global.presentation.api.common.ApiResponseCode;
import com.wanted.momocity.report.application.usecase.ReportCommandUseCase;
import com.wanted.momocity.report.presentation.api.request.SubmitReportRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/* comment.
    ReportController 정리
    MS-20 신고 접수 : 로그인한 회원 화원이 신고 버튼 누를 때 호출하는 컨트롤러
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/reports")
@Tag(name = "Report", description = "회원 신고 접수 API")
public class ReportController {

    private final ReportCommandUseCase reportCommandUseCase;

    @PostMapping
    @PreAuthorize("isAuthenticated()") // 신고는 로그인 회원이면 누구나 (직군 제한 없음) — 인가를 어노테이션으로 명시
    @Operation(
            summary = "신고 접수",
            description = "로그인한 회원이 게시글/댓글/강의/채택 등을 신고한다."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "201", description = "신고 접수 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400", description = "요청 본문 검증 실패"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "401", description = "인증 토큰 누락 또는 만료")
    })
    // 반환 타입이 SubmitReportResponse 가 MS-20 특성 상 data 없이 message 만 반환이라 필요없어졌음
    public ResponseEntity<ApiResponse<Void>> submitReport(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody SubmitReportRequest request
    ) {
        // SecurityContext에서 신고자 userId 추출
        Long reporterUserId = userDetails.getUserId();

        // Request → Command 변환 후 UseCase 호출
        reportCommandUseCase.submitReport(request.toCommand(reporterUserId));

        // 201 Created — data 없이 message만 반환 (MS-20 스펙)
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.created(
                        ApiResponseCode.CREATED,
                        "신고가 접수되었습니다.",
                        null
                ));
    }
}