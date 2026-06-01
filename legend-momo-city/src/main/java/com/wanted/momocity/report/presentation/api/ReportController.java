package com.wanted.momocity.report.presentation.api;

import com.wanted.momocity.auth.infrastructure.security.CustomUserDetails;
import com.wanted.momocity.global.presentation.api.common.ApiResponse;
import com.wanted.momocity.global.presentation.api.common.ApiResponseCode;
import com.wanted.momocity.report.application.command.SubmitReportCommand;
import com.wanted.momocity.report.application.usecase.ReportCommandUseCase;
import com.wanted.momocity.report.domain.model.Report;
import com.wanted.momocity.report.presentation.api.request.SubmitReportRequest;
import com.wanted.momocity.report.presentation.api.response.SubmitReportResponse;
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
    1. 역할 : 신고 접수 HTTP API 진입점이다. 로그인한 회원이 다른 회원/콘텐츠를 신고할 때 호출한다.
    2. 다루는 API :
        - Post /api/v1/reports
    3. 클래스 레벨 어노테이션
        - @RestController : REST API 컨트롤러. 반환값 자동으로 JSON 직렬화
        - @RequiredArgsConstructor : final 필드 자동 생성
        - @RequestMapping : 클래스 내 모든 핸들러의 공통 URL
        - @Tag : Swagger UI 에서 Report 그룹으로 묶어서 표시
    4. 의존성
        - ReportCommandUseCase : UseCase 인터페이스 의존, 구현체에 대해서 모른다.
    5. submitReport 처리 흐름 5단계
        a) Authentication.getName() 으로 신고자 email 추출
        b) request.toCommand(email) 로 Request DTO → Command 변환
        c) reportCommandUseCase.submitReport(command) 호출 → 도메인 Report 반환
        d) SubmitReportResponse.from(report) 로 도메인 → 응답 DTO 변환
        e) ResponseEntity 로 201 Created + 공통 응답 wrapper 묶어서 반환
    6. WHY 응답 201 Created
       → REST 컨벤션 - 신규 리소스 생성 성공 시 201
       → 200 OK 와 구분을 지어놓는다. client 가 응답만 보고 새로 생겼다고 인지
    7. WHY Authentication 으로 email 추출
       → 보안 - client 가 보낸 email 신뢰 불가능
       → Spring Security 가 JWT/세션에서 검증한 인증된 email 만 사용
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
            description = "로그인한 회원이 게시글/댓글/회원/강의를 신고한다."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "201", description = "신고 접수 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400", description = "요청 본문 검증 실패"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "401", description = "인증 토큰 누락 또는 만료")
    })
    public ResponseEntity<ApiResponse<SubmitReportResponse>> submitReport(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody SubmitReportRequest request
    ) {
        // 1. 인증 principal 에서 신고자 userId 추출 (FriendController 와 동일 방식)
        Long reporterUserId = userDetails.getUserId();

        // 2. Request → Command 변환 (Request 의 toCommand 메서드 활용)
        SubmitReportCommand command = request.toCommand(reporterUserId);

        // 3. UseCase 호출 → 도메인 객체 반환
        Report report = reportCommandUseCase.submitReport(command);

        // 4. 도메인 → 응답 DTO 변환
        SubmitReportResponse response = SubmitReportResponse.from(report);

        // 5. 201 Created + 공통 응답 wrapper 로 반환
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.created(
                        ApiResponseCode.CREATED,
                        "신고가 접수되었습니다.",
                        response
                ));
    }
}