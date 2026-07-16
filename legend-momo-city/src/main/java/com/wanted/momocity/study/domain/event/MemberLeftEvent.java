package com.wanted.momocity.study.domain.event;

/*
 * comment.
 *  그룹방 멤버 자진 퇴장 이벤트
 *  - infrastructure.event.StudyBroadcastEventHandler가 수신 -> STOMP로 방 토픽 브로드캐스트
 * */
public record MemberLeftEvent(
        Long roomId,
        Long userId
) {
}