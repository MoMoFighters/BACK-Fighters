package com.wanted.momocity.study.presentation.api.response;

import java.time.LocalDateTime;

/*
 * comment.
 *  솔로 세션 종료 API가 사용하는 응답 DTO
 *  - 사용 API :
 *      POST /api/v3/study/solo/end
 *  -
 *  totalSeconds : end 시점의 최종 확정 누적값 (start/pause/resume 시엔 0, end 응답에서만 의미 있음)
 *  endTime : end 응답에서만 값 존재
 * */

public record SoloSessionEndResponse(
        Long sessionId,
        String status,
        int totalSeconds,
        LocalDateTime endTime
) {
}