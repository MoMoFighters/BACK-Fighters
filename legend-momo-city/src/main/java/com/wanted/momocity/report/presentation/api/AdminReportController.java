package com.wanted.momocity.report.presentation.api;

import com.wanted.momocity.global.presentation.api.common.ApiResponse;
import com.wanted.momocity.global.presentation.api.common.ApiResponseCode;
import com.wanted.momocity.report.application.usecase.ReportQueryUseCase;
import com.wanted.momocity.report.domain.model.ReportStatus;
import com.wanted.momocity.report.presentation.api.response.ReportListResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/* comment.
    AdminReportController 정리
    1. 역할 : 관리자 신고 목록 조회 HTTP API 진입점 (ADMIN 권한 가진 사용자만 접근 가능)
    2. 다루는 API : GET /api/v1/reports?limit=N&status=PENDING
    3. 클래스 레벨 어노테이션
       - @RestController : REST API 컨트롤러. 반환값 자동 JSON 직렬화.
       - @RequiredArgsConstructor : final 필드 생성자 자동 생성 (Lombok).
       - @RequestMapping("/api/v1/reports") : 공통 URL prefix.
       - @PreAuthorize("hasRole('ADMIN')") : 모든 핸들러 호출 전 ADMIN 권한 검사. 미충족 시 403.
       - @Tag : Swagger UI 에서 "Admin - 신고" 그룹.
    4. 의존성
        - ReportQueryUseCase : UseCase 인터페이스 의존. 구현체는 ReportQueryService 모르고있는 상태
    5. getReports 처리 흐름 4단계
        a) HTTP 요청 받기 (limit + status query param)
        b) status null 여부에 따라 getRecent 또는 getByStatus 선택 호출
        c) UseCase 출력(ReportList) → ReportListResponse.from() 으로 응답 DTO 변환
        d) ResponseEntity.ok() + 공통 응답 wrapper 로 반환
    6. WHY status 파라미터가 optional (required = false)
       → 호출자 의도 에 따라 두 가지 사용 패턴이 지원된다
            1. status 없음 : 전체 최근 N개
            2. status 있음 : 특정 상태의 최근 N개
       → 한 엔드포인트를 통해서 두 케이스를 처리한다.
    7. WHY ReportController 와 별도 컨트롤러 (같은 base path 공유)
       → 권한 정책이 다르기 때문에 클래스 레벨에서 분리하면 가독성 및 유지보수가 좋아진다.
       → POST /api/v1/reports(ReportController, 회원) 와 base path 는 같지만,
         스프링은 (경로 + HTTP 메서드) 조합으로 매핑하므로 GET/POST 가 충돌하지 않는다.
       → 결과 : 하나의 리소스 경로(reports)를 권한/CQRS(Command·Query)로 안전하게 분리.
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/reports")
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "Admin - 신고", description = "관리자 신고 목록 조회")
public class AdminReportController {

    private final ReportQueryUseCase reportQueryUseCase;

    @GetMapping
    @Operation(
            summary = "관리자 신고 목록 조회",
            description = "최근 N개의 신고를 조회한다. status 파라미터로 상태별 필터링 가능."
    )
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
            @Parameter(description = "신고 상태 필터 (선택)", example = "PENDING")
            @RequestParam(required = false) ReportStatus status
    ) {
        // 1. status 유무에 따라 UseCase 메서드 선택
        ReportQueryUseCase.ReportList list = (status == null)
                ? reportQueryUseCase.getRecent(limit)
                : reportQueryUseCase.getByStatus(status, limit);

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
}