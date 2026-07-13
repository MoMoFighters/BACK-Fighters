package com.wanted.momocity.study.presentation.api.response;

/*
 * comment.
 *  그룹방 내 타이머 종료 API가 공용으로 사용하는 응답 DTO
 *  - 사용 API :
 *      POST /api/v3/study/rooms/{roomId}/members/timer/end
 * */

public record TimerEndResponse(
        Long roomId,
        Long memberId,
        int totalSeconds
) {
}