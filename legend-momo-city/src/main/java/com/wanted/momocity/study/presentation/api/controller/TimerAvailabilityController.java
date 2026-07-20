package com.wanted.momocity.study.presentation.api.controller;

import com.wanted.momocity.auth.infrastructure.security.CustomUserDetails;
import com.wanted.momocity.global.presentation.api.common.ApiResponse;
import com.wanted.momocity.study.application.common.usecase.TimerAvailabilityUseCase;
import com.wanted.momocity.study.presentation.api.common.StudyResponseCode;
import com.wanted.momocity.study.presentation.api.response.common.TimerAvailabilityResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "TimerAvailability", description = "Study(열품타) 도메인 - 타이머 시작 가능 여부 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v3/study")
public class TimerAvailabilityController {

    private final TimerAvailabilityUseCase timerAvailabilityUseCase;

    // 타이머 시작 가능 여부 조회
    @Operation(summary = "타이머 시작 가능 여부 조회", description = "지금 새로운 타이머(솔로/그룹)를 시작할 수 있는 상태인지 조회합니다.")
    @GetMapping("/timer-availability")
    public ResponseEntity<ApiResponse<TimerAvailabilityResponse>> getAvailability(
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                StudyResponseCode.TIMER_AVAILABILITY_FETCHED,
                "타이머 시작 가능 여부를 조회했습니다.",
                timerAvailabilityUseCase.getAvailability(userDetails.getUserId())
        ));
    }

}
