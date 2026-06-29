package com.wanted.momocity.admin.presentation.api;

import com.wanted.momocity.admin.application.usecase.AccessLogQueryUseCase;
import com.wanted.momocity.admin.domain.access.AccessLogAction;
import com.wanted.momocity.admin.presentation.api.response.AccessLogResponse;
import com.wanted.momocity.global.presentation.api.common.ApiResponse;
import com.wanted.momocity.global.presentation.api.common.ApiResponseCode;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/* comment.
    MS-4 접근 로그 목록 조회 API. ADMIN 전용.
    action 파라미터 유무로 전체 조회 / 필터 조회를 분기한다.
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/logs")
// 관리자만 봐야하니까
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "Admin - 접근 로그", description = "관리자 접근 로그 조회")
public class AccessLogController {

    // Final 선언으로 불변성 선언
    private final AccessLogQueryUseCase accessLogQueryUseCase;

    @GetMapping("/access")
    @Operation(
            summary = "접근 로그 목록 조회",
            description = "접근 로그를 page/limit 기준으로 조회한다. action 파라미터로 필터링 가능"
    )
    // 1페이지에 10개의 내용만 들어갈 수 있게 제한
    public ResponseEntity<ApiResponse<AccessLogResponse>> getAccessLogs(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int limit,
            @RequestParam(required = false) AccessLogAction action
    ) {
        // action 유무에 따라 UseCase 메서드 분기
        AccessLogQueryUseCase.AccessLogResult result = (action == null)
                ? accessLogQueryUseCase.getAll(page, limit)
                : accessLogQueryUseCase.getByAction(action, page, limit);

        // Page<AccessLog> + userInfoMap → 응답 DTO 변환
        AccessLogResponse response = AccessLogResponse.from(result.page(), result.userInfoMap());

        // 문제 없다면 200을 띄우며 성공했다고 알리기
        return ResponseEntity.ok(
                ApiResponse.success(ApiResponseCode.SUCCESS, "접근 로그 목록 조회 성공", response)
        );
    }
}