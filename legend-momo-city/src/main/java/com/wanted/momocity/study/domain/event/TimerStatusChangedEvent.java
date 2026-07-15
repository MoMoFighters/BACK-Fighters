package com.wanted.momocity.study.domain.event;

import com.wanted.momocity.study.domain.model.GroupRoomMember;

/*
 * comment.
 *  그룹방 내 개인 타이머 상태 변경 이벤트 (start/pause/end 전부 이걸로 통일)
 *  - infrastructure.event.StudyBroadcastEventHandler가 수신 -> STOMP로 방 토픽 브로드캐스트
 *  - 본인의 다른 탭/기기도 같은 방 토픽을 구독하므로, 별도 유저 큐 없이 이 이벤트 하나로 동기화
 *  -
 *  timerStatus가 null이면 "타이머 종료(endTimer)"를 의미
 * */
public record TimerStatusChangedEvent(
        Long roomId,
        Long userId,
        GroupRoomMember.TimerStatus timerStatus
) {
}