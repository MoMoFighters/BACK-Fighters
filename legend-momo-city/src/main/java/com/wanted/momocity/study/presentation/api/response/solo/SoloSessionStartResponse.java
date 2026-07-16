package com.wanted.momocity.study.presentation.api.response.solo;

import com.wanted.momocity.study.presentation.api.response.common.LapItem;

import java.time.LocalDateTime;

/*
 * comment.
 *  솔로 세션 시작 / 재개  API가 사용하는 응답 DTO
 *  - 사용 API :
 *      POST /api/v3/study/solo/start
 *  -
 *  action : "STARTED" / "RESUMED" / "PAUSED" / "ENDED" (SoloSessionResult 참고)
 *  startTime : 세션 생성 시각
 * */

public record SoloSessionStartResponse(
        Long sessionId,
        String status,
        String action,
        LocalDateTime startTime,
        int accumulatedSeconds,
        LapItem lap
) {
}