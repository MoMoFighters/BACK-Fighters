package com.wanted.momocity.admin.presentation.api;

import com.wanted.momocity.admin.application.usecase.ErrorLogQueryUseCase;
import com.wanted.momocity.admin.presentation.api.response.ErrorLogResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/* comment.
    ErrorLogController 정리
    1. 이 클래스의 역할 : 에러 로그 조회 HTTP API 진입점. FE 대시보드 에러 로그 위젯이 호출
    2. 다루는 API :
        - GET /api/v1/admin/error-logs?limit=N
    3. 클래스 레벨 어노테이션 :
        - @RestController              : REST API 컨트롤러 표시. 반환값 자동 JSON 직렬화
        - @RequestMapping("/api/v1/admin") : 클래스 내 모든 핸들러의 공통 URL prefix
        - @PreAuthorize("hasRole('ADMIN')") : 모든 핸들러 호출 전 ADMIN 권한 검사. 미충족 시 403
        - @Tag                         : Swagger UI 에서 "Admin - 에러 로그" 그룹으로 묶어 표시
    4. 의존성 :
        - ErrorLogQueryUseCase : UseCase 인터페이스 의존 (DIP). 구현체(Service) 모름
    5. Controller 책임 5단계 :
        a) HTTP 요청 받기 (limit query param)
        b) UseCase 호출
        c) Result → Item DTO 변환 (stream map)
        d) ErrorLogResponse 로 묶기
        e) ResponseEntity 로 반환
    6. MS-6 / 대시보드 Controller 와 차이 :
        - HTTP 메서드 : GET (조회)
        - 입력 : @RequestParam (쿼리 파라미터), path variable / body 없음
 */
@RestController
@RequestMapping("/api/v1/admin")
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "Admin - 에러 로그", description = "관리자 대시보드 에러 로그 조회")
public class ErrorLogController {

    private final ErrorLogQueryUseCase errorLogQueryUseCase;

    public ErrorLogController(ErrorLogQueryUseCase errorLogQueryUseCase) {
        this.errorLogQueryUseCase = errorLogQueryUseCase;
    }

    /* comment.
        실제 구현 시 흐름 (m03 우선순위) :
        1. ErrorLogList result = errorLogQueryUseCase.getRecent(limit);
        2. List<Item> items = result.errorLogs().stream()
                                    .map(this::toItem)
                                    .toList();
        3. ErrorLogResponse response = new ErrorLogResponse(items);
        4. return ResponseEntity.ok(response);

        // 헬퍼 (private Item toItem(ErrorLog log))
        // return new Item(
        //     log.getId(),
        //     log.getLevel().name(),   // enum → String
        //     log.getSource(),
        //     log.getMessage(),
        //     log.getOccurredAt()
        // );
     */
    @GetMapping("/error-logs")
    @Operation(
            summary = "관리자 에러 로그 조회",
            description = "최근 N개의 에러 로그를 조회한다. FE 대시보드 에러 로그 위젯이 호출."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "에러 로그 조회 성공"),
            @ApiResponse(responseCode = "401", description = "인증 토큰 누락 또는 만료"),
            @ApiResponse(responseCode = "403", description = "ADMIN 권한 없음")
    })
    public ResponseEntity<ErrorLogResponse> getRecentErrorLogs(
            @Parameter(description = "조회할 최대 개수", example = "10")
            @RequestParam(defaultValue = "10") int limit
    ) {
        throw new UnsupportedOperationException("TODO: m03 우선순위 - 에러 로그 조회 컨트롤러 구현");
    }
}