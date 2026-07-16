package com.wanted.momocity.study.domain.event;

/*
 * comment.
 *  그룹방 방장 자동 위임 이벤트 (방장이 나가서 다음 입장자에게 위임될 때)
 *  - infrastructure.event.StudyBroadcastEventHandler가 수신 -> STOMP로 방 토픽 브로드캐스트
 * */
public record HostChangedEvent(
        Long roomId,
        Long newHostUserId
) {
}