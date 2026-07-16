package com.wanted.momocity.study.domain.event;

/*
 * comment.
 *  그룹방 멤버 입장 이벤트 (초대 수락 시 발행)
 *  - infrastructure.event.StudyBroadcastEventHandler가 수신 -> STOMP로 방 토픽 브로드캐스트
 * */
public record MemberJoinedEvent(
        Long roomId,
        Long userId
) {
}