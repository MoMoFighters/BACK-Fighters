package com.wanted.momocity.study.presentation.api.response;

import java.time.LocalDateTime;

/*
 * comment.
 *  솔로 세션 일시정지 API가 사용하는 응답 DTO
 *  - 사용 API :
 *      POST /api/v3/study/solo/pause
 *  -
 *  accumulatedSeconds : pause 시점까지의 누적값
 * */

public record SoloSessionPauseResponse(
        Long sessionId,
        String status,
        int accumulatedSeconds
) {
}