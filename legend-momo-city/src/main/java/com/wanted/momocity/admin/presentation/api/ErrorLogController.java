package com.wanted.momocity.admin.presentation.api;

import com.wanted.momocity.admin.application.usecase.ErrorLogQueryUseCase;
import com.wanted.momocity.admin.domain.audit.ErrorLog;
import com.wanted.momocity.admin.presentation.api.response.ErrorLogResponse;
import com.wanted.momocity.global.presentation.api.common.ApiResponse;
import com.wanted.momocity.global.presentation.api.common.ApiResponseCode;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/* comment.
    ErrorLogController 정리
    1. 이 클래스의 역할 : 에러 로그 조회 HTTP API 진입점. FE 대시보드 에러 로그 위젯이 호출
    2. 다루는 API :
        - GET /api/v1/error-logs?limit=N
    3. 클래스 레벨 어노테이션 :
        - @RestController              : REST API 컨트롤러 표시. 반환값 자동 JSON 직렬화
        - @RequestMapping("/api/v1") : 클래스 내 모든 핸들러의 공통 URL prefix
        - @PreAuthorize("hasRole('ADMIN')") : 모든 핸들러 호출 전 ADMIN 권한 검사. 미충족 시 403
        - @Tag                         : Swagger UI 에서 "Admin - 에러 로그" 그룹으로 묶어 표시
    4. 의존성 :
        - ErrorLogQueryUseCase : UseCase 인터페이스 의존 (DIP). 구현체(Service) 모름
    5. Controller 책임 5단계 :
        a) HTTP 요청 받기 (limit query param)
        b) UseCase 호출
        c) Result → Item DTO 변환 (stream map)
        d) ErrorLogResponse 로 묶기
        e) ApiResponse wrapper 로 감싸 ResponseEntity 반환 (전 컨트롤러 일관성)
 */
@RestController
@RequestMapping("/api/v1")
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "Admin - 에러 로그", description = "관리자 대시보드 에러 로그 조회")
public class ErrorLogController {

    private final ErrorLogQueryUseCase errorLogQueryUseCase;

    public ErrorLogController(ErrorLogQueryUseCase errorLogQueryUseCase) {
        this.errorLogQueryUseCase = errorLogQueryUseCase;
    }

    @GetMapping("/error-logs")
    @Operation(
            summary = "관리자 에러 로그 조회",
            description = "최근 N개의 에러 로그를 조회한다. FE 대시보드 에러 로그 위젯이 호출."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200", description = "에러 로그 조회 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "401", description = "인증 토큰 누락 또는 만료"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "403", description = "ADMIN 권한 없음")
    })
    public ResponseEntity<ApiResponse<ErrorLogResponse>> getRecentErrorLogs(
            @Parameter(description = "조회할 최대 개수", example = "10")
            @RequestParam(defaultValue = "10") int limit
    ) {
        // 1. UseCase 호출 → 도메인 리스트 받기
        ErrorLogQueryUseCase.ErrorLogList result = errorLogQueryUseCase.getRecent(limit);

        // 2. 도메인 → 응답 Item 변환 (stream map)
        List<ErrorLogResponse.Item> items = result.errorLogs().stream()
                .map(this::toItem)
                .toList();

        // 3. ApiResponse wrapper 로 감싸 200 OK 반환
        return ResponseEntity.ok(
                ApiResponse.success(
                        ApiResponseCode.SUCCESS,
                        "에러 로그 조회 성공",
                        new ErrorLogResponse(items)
                )
        );
    }

    // 도메인 ErrorLog → 응답 Item 변환 헬퍼 (enum → String)
    private ErrorLogResponse.Item toItem(ErrorLog log) {
        return new ErrorLogResponse.Item(
                log.getId(),
                log.getLevel().name(),
                log.getSource(),
                log.getMessage(),
                log.getOccurredAt()
        );
    }
}
