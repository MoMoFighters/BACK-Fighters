package com.wanted.momocity.study.presentation.api.response.common;

/*
 * comment.
 *  타이머 시작 가능 여부 응답 DTO
 *  - 사용 API : GET /api/v3/study/timer-availability
 * */

public record TimerAvailabilityResponse(
        boolean canStartTimer
) {
}
