package com.wanted.momocity.study.application.member.timer.result;

import com.wanted.momocity.study.domain.model.GroupRoomMember;

import java.time.LocalDateTime;

/*
 * comment.
 *  그룹방 내 타이머 시작/일시정지/종료 공통 결과 DTO
 *  -
 *  action : STARTED(신규 시작) / RESUMED(재개) / PAUSED / ENDED
 *  -> start API 하나가 "신규 시작"과 "재개"를 함께 처리
 *  -
 *  accumulatedSeconds : STARTED/RESUMED 시엔 재개 시점까지의 기존 누적값(0 또는 이전 값),
 *                        PAUSED/ENDED 시엔 이번에 갱신된 최종 누적값
 * */

public record TimerActionResult(
        Long roomId,
        Long memberId,
        Action action,
        GroupRoomMember.TimerStatus timerStatus,  // 종료(ENDED) 시에는 null
        LocalDateTime startedAt,                  // start(재개 포함) 시에만 값 존재, pause/end는 null
        int accumulatedSeconds
) {

    public static TimerActionResult ofStarted(GroupRoomMember member, boolean wasResumed) {
        return new TimerActionResult(
                member.getGroupRoomId(), member.getId(),
                wasResumed ? Action.RESUMED : Action.STARTED,
                member.getTimerStatus(), member.getLastResumedAt(), member.getTotalSeconds()
        );
    }

    public static TimerActionResult ofPaused(GroupRoomMember member) {
        return new TimerActionResult(
                member.getGroupRoomId(), member.getId(),
                Action.PAUSED, member.getTimerStatus(), null, member.getTotalSeconds()
        );
    }

    public static TimerActionResult ofEnded(GroupRoomMember member) {
        return new TimerActionResult(
                member.getGroupRoomId(), member.getId(),
                Action.ENDED, null, null, member.getTotalSeconds()
        );
    }

    public enum Action {
        STARTED, RESUMED, PAUSED, ENDED
    }

}
