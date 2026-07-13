package com.wanted.momocity.study.presentation.api.response;

/*
 * comment.
 *  그룹방 내 타이머 시작 / 재개 API가 사용하는 응답 DTO
 *  - 사용 API :
 *      POST /api/v3/study/rooms/{roomId}/members/timer/start
 *  -
 *  action : "STARTED"(신규 시작) / "RESUMED"(재개) / "PAUSED" / "ENDED"
 *  -> start API 하나가 신규 시작과 재개를 함께 처리하므로, 프론트가 버튼 라벨/토스트 문구를
 *     다르게 보여줄 수 있도록 이 필드로 구분해서 내려줌 (application.member.result.TimerActionResult 참고)
 * */

import java.time.LocalDateTime;

public record TimerStartResponse(
        Long roomId,
        Long memberId,
        String action,
        String timerStatus,
        LocalDateTime startedAt,
        int accumulatedSeconds
) {
}