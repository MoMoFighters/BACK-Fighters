package com.wanted.momocity.study.application.solo.result;

import com.wanted.momocity.study.domain.model.SoloSession;

import java.time.LocalDateTime;

/*
 * comment.
 *  솔로 세션 시작/일시정지/재개/종료 공통 결과 DTO
 *  - Result는 하나로 공용화하고, Controller가 각 API에 맞는 슬림한 Response
 *    (SoloSessionStartResponse/PauseResponse/EndResponse)로 필요한 필드만 골라 담는다.
 *  -
 *  action : STARTED(신규 시작) / RESUMED(재개) / PAUSED / ENDED
 *  -> solo/start API 하나가 신규 시작과 재개를 함께 처리하므로, member.timer와 동일하게
 *     프론트가 버튼 라벨/토스트 문구를 다르게 보여줄 수 있도록 이 필드로 구분해서 내려줌
 *  -
 *  startTime : start(재개 포함) 응답에서만 의미 있음
 *  accumulatedSeconds : pause 시점까지의 중간 누적값 (start/resume 시엔 재개 시점까지의 기존값)
 *  totalSeconds : end 시점의 최종 확정 누적값 (start/pause/resume 시엔 의미 없음)
 *  endTime : end 응답에서만 값 존재
 * */

public record SoloActionResult(
        Long sessionId,
        SoloSession.SoloSessionStatus status,
        Action action,
        LocalDateTime startTime,
        int accumulatedSeconds,
        int totalSeconds,
        LocalDateTime endTime
) {
    public static SoloActionResult ofStarted(SoloSession session, boolean wasResumed) {
        return new SoloActionResult(
                session.getId(), session.getStatus(),
                wasResumed ? Action.RESUMED : Action.STARTED,
                session.getStartTime(), session.getTotalSeconds(), 0, null
        );
    }

    public static SoloActionResult ofPaused(SoloSession session) {
        return new SoloActionResult(
                session.getId(), session.getStatus(), Action.PAUSED,
                null, session.getTotalSeconds(), 0, null
        );
    }

    public static SoloActionResult ofEnded(SoloSession session) {
        return new SoloActionResult(
                session.getId(), session.getStatus(), Action.ENDED,
                null, 0, session.getTotalSeconds(), session.getEndTime()
        );
    }

    public enum Action {
        STARTED, RESUMED, PAUSED, ENDED
    }
}
