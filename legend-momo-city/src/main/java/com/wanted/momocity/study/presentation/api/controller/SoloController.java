package com.wanted.momocity.study.presentation.api.controller;

import com.wanted.momocity.auth.infrastructure.security.CustomUserDetails;
import com.wanted.momocity.global.presentation.api.common.ApiResponse;
import com.wanted.momocity.study.application.solo.result.SoloActionResult;
import com.wanted.momocity.study.application.solo.usecase.SoloCommandUseCase;
import com.wanted.momocity.study.application.solo.usecase.SoloQueryUseCase;
import com.wanted.momocity.study.presentation.api.common.StudyResponseCode;
import com.wanted.momocity.study.presentation.api.response.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

/*
 * comment.
 *  솔로 세션 HTTP 요청 처리
 *  - 비즈니스 로직 없음, UseCase 호출 + Result/Response 변환만 담당
 *  -
 *  start/pause/end는 SoloActionResult(공용) -> 각 API 전용 슬림 Response로 Controller가 조립
 * */

@Tag(name = "Solo", description = "Study(열품타) 도메인 - 솔로 세션 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v3/study/solo")
public class SoloController {

    private final SoloCommandUseCase soloCommandUseCase;
    private final SoloQueryUseCase soloQueryUseCase;

    // 솔로 세션 시작 (신규 시작 + 재개 통합)
    @Operation(summary = "솔로 세션 시작", description = "솔로 타이머를 시작하거나(신규) 재개합니다.")
    @PostMapping("/start")
    public ResponseEntity<ApiResponse<SoloSessionStartResponse>> start(
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        SoloActionResult result = soloCommandUseCase.start(userDetails.getUserId());

        return ResponseEntity.ok(ApiResponse.success(
                StudyResponseCode.SOLO_STARTED,
                result.action() == SoloActionResult.Action.RESUMED ? "솔로 타이머를 재개했습니다." : "솔로 타이머를 시작했습니다.",
                new SoloSessionStartResponse(
                        result.sessionId(), result.status().name(), result.action().name(),
                        result.startTime(), result.accumulatedSeconds()
                )
        ));
    }

    // 솔로 세션 일시정지
    @Operation(summary = "솔로 세션 일시정지", description = "솔로 타이머를 일시정지합니다.")
    @PostMapping("/pause")
    public ResponseEntity<ApiResponse<SoloSessionPauseResponse>> pause(
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        SoloActionResult result = soloCommandUseCase.pause(userDetails.getUserId());

        return ResponseEntity.ok(ApiResponse.success(
                StudyResponseCode.SOLO_PAUSED,
                "솔로 타이머를 일시정지했습니다.",
                new SoloSessionPauseResponse(result.sessionId(), result.status().name(), result.accumulatedSeconds())
        ));
    }

    // 솔로 세션 종료 (최종 확정)
    @Operation(summary = "솔로 세션 종료", description = "솔로 타이머를 종료하고 최종 시간을 확정합니다.")
    @PostMapping("/end")
    public ResponseEntity<ApiResponse<SoloSessionEndResponse>> end(
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        SoloActionResult result = soloCommandUseCase.end(userDetails.getUserId());

        return ResponseEntity.ok(ApiResponse.success(
                StudyResponseCode.SOLO_ENDED,
                "솔로 타이머를 종료했습니다.",
                new SoloSessionEndResponse(result.sessionId(), result.status().name(), result.totalSeconds(), result.endTime())
        ));
    }

    // 현재 진행 중인 솔로 세션 조회 (화면 복구용)
    @Operation(summary = "현재 진행 중인 솔로 세션 조회", description = "새로고침/재접속 시 화면 복구용으로 사용합니다.")
    @GetMapping("/current")
    public ResponseEntity<ApiResponse<SoloCurrentResponse>> current(
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        var current = soloQueryUseCase.getCurrent(userDetails.getUserId());

        // 진행 중인 세션이 없는 것은 정상 상태이므로, 별도 코드/메시지로 구분해서 200 응답
        // ApiResponse.success(code, message)는 ApiResponse<Void>를 반환하므로,
        // 메서드 반환 타입(ApiResponse<SoloCurrentResponse>)과 맞추기 위해 3개 인자 버전에 null을 명시적으로 전달
        if (current.isEmpty()) {
            return ResponseEntity.ok(ApiResponse.success(
                    StudyResponseCode.SOLO_CURRENT_EMPTY,
                    "진행 중인 세션이 없습니다.",
                    (SoloCurrentResponse) null
            ));
        }

        return ResponseEntity.ok(ApiResponse.success(
                StudyResponseCode.SOLO_CURRENT_FETCHED,
                "현재 진행 중인 세션을 조회했습니다.",
                current.get()
        ));
    }

    // 솔로 세션 이력 조회
    @Operation(summary = "솔로 세션 이력 조회", description = "종료된 솔로 세션 목록을 최신순으로 조회합니다.")
    @GetMapping("/history")
    public ResponseEntity<ApiResponse<SoloHistoryResponse>> history(
            @RequestParam(required = false) Long cursor,
            @RequestParam(defaultValue = "20") int size,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                StudyResponseCode.SOLO_HISTORY_FETCHED,
                "솔로 세션 이력을 조회했습니다.",
                soloQueryUseCase.getHistory(userDetails.getUserId(), cursor, size)
        ));
    }

}
