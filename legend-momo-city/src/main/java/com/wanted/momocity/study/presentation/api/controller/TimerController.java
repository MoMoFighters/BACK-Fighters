package com.wanted.momocity.study.presentation.api.controller;

import com.wanted.momocity.auth.infrastructure.security.CustomUserDetails;
import com.wanted.momocity.global.presentation.api.common.ApiResponse;
import com.wanted.momocity.study.application.member.timer.result.TimerActionResult;
import com.wanted.momocity.study.application.member.timer.usecase.TimerCommandUseCase;
import com.wanted.momocity.study.application.member.timer.usecase.TimerQueryUseCase;
import com.wanted.momocity.study.presentation.api.common.StudyResponseCode;
import com.wanted.momocity.study.presentation.api.response.member.timer.MemberLapListResponse;
import com.wanted.momocity.study.presentation.api.response.member.timer.TimerEndResponse;
import com.wanted.momocity.study.presentation.api.response.member.timer.TimerPauseResponse;
import com.wanted.momocity.study.presentation.api.response.member.timer.TimerStartResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

/*
 * comment.
 *  그룹방 내 개인 타이머 HTTP 요청 처리
 *  - 비즈니스 로직 없음, UseCase 호출 + Result -> Response 변환만 담당
 *  -
 *  MemberController에 있던 timer/start, timer/pause, timer/end 3개 엔드포인트 이동
 *  URL 경로(/api/v3/study/rooms/{roomId}/members/timer/...)는 팀이 이미 확정한 API 스펙 그대로 유지
 *  서버 내부 클래스 구조(패키지/컨트롤러)만 분리
 * */

@Tag(name = "Timer", description = "Study(열품타) 도메인 - 그룹방 내 개인 타이머 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v3/study/rooms/{roomId}/members/timer")
public class TimerController {

    private final TimerCommandUseCase timerCommandUseCase;
    private final TimerQueryUseCase timerQueryUseCase;

    // 타이머 시작 (신규 시작 + 재개 통합)
    @Operation(summary = "그룹방 타이머 시작", description = "본인 타이머를 시작하거나(신규) 재개합니다.")
    @PostMapping("/start")
    public ResponseEntity<ApiResponse<TimerStartResponse>> start(
            @PathVariable Long roomId,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        TimerActionResult result = timerCommandUseCase.start(userDetails.getUserId(), roomId);

        return ResponseEntity.ok(ApiResponse.success(
                StudyResponseCode.TIMER_STARTED,
                // action에 따라 문구를 다르게 내려줘서 프론트가 토스트 텍스트를 그대로 써도 되게 함
                result.action() == TimerActionResult.Action.RESUMED ? "타이머를 재개했습니다." : "타이머를 시작했습니다.",
                new TimerStartResponse(
                        result.roomId(), result.memberId(), result.action().name(),
                        result.timerStatus().name(), result.startedAt(), result.accumulatedSeconds(), result.lap()
                )
        ));
    }

    // 타이머 일시정지
    @Operation(summary = "그룹방 타이머 일시정지", description = "본인 타이머를 일시정지합니다.")
    @PostMapping("/pause")
    public ResponseEntity<ApiResponse<TimerPauseResponse>> pause(
            @PathVariable Long roomId,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        TimerActionResult result = timerCommandUseCase.pause(userDetails.getUserId(), roomId);

        return ResponseEntity.ok(ApiResponse.success(
                StudyResponseCode.TIMER_PAUSED,
                "타이머를 일시정지했습니다.",
                new TimerPauseResponse(
                        result.roomId(), result.memberId(),
                        result.timerStatus().name(), result.accumulatedSeconds(), result.lap()
                )
        ));
    }

    // 타이머 완전 종료 (방은 유지)
    @Operation(summary = "그룹방 타이머 종료", description = "본인 타이머를 완전히 종료합니다. 방은 유지됩니다.")
    @PostMapping("/end")
    public ResponseEntity<ApiResponse<TimerEndResponse>> end(
            @PathVariable Long roomId,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        TimerActionResult result = timerCommandUseCase.end(userDetails.getUserId(), roomId);

        return ResponseEntity.ok(ApiResponse.success(
                StudyResponseCode.TIMER_ENDED,
                "타이머를 종료했습니다.",
                new TimerEndResponse(result.roomId(), result.memberId(), result.accumulatedSeconds(), result.lap())
        ));
    }

    // 메서드 추가
    // 특정 멤버의 랩 목록 조회 (카드 클릭 시)
    @Operation(summary = "그룹방 멤버 랩 목록 조회", description = "특정 멤버의 랩 목록을 조회합니다. 본인 것뿐 아니라 다른 멤버 것도 조회 가능합니다.")
    @GetMapping("/{targetUserId}/laps")
    public ResponseEntity<ApiResponse<MemberLapListResponse>> memberLaps(
            @PathVariable Long roomId,
            @PathVariable Long targetUserId,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                StudyResponseCode.TIMER_LAPS_FETCHED,
                "멤버 랩 목록을 조회했습니다.",
                timerQueryUseCase.getMemberLaps(userDetails.getUserId(), roomId, targetUserId)
        ));
    }

    // 내 랩 목록 조회
    @Operation(summary = "내 랩 목록 조회", description = "로그인한 유저 본인의 이 방에서의 랩 목록을 조회합니다.")
    @GetMapping("/me/laps")
    public ResponseEntity<ApiResponse<MemberLapListResponse>> myLaps(
            @PathVariable Long roomId,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        Long userId = userDetails.getUserId();

        return ResponseEntity.ok(ApiResponse.success(
                StudyResponseCode.MY_LAPS_FETCHED,
                "내 랩 목록을 조회했습니다.",
                timerQueryUseCase.getMemberLaps(userId, roomId, userId)
        ));
    }

}