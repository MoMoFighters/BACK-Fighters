package com.wanted.momocity.study.presentation.api.response;

/*
 * comment.
 *  그룹방 내 타이머 일시정지 API가 사용하는 응답 DTO
 *  - 사용 API :
 *      POST /api/v3/study/rooms/{roomId}/members/timer/pause
 *  -
 *  timerStatus : end 호출 시에는 null (타이머 완전 종료 상태를 의미)
 *  accumulatedSeconds :  pause/end 호출 시에만 실제 누적값
 * */

public record TimerPauseResponse(
        Long roomId,
        Long memberId,
        String timerStatus,
        int accumulatedSeconds
) {
}