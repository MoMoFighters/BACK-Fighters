package com.wanted.momocity.study.domain.event;

/*
 * comment.
 *  그룹방 종료 이벤트 (인원 0명이 되어 소프트딜리트 처리될 때)
 *  - infrastructure.event.StudyBroadcastEventHandler가 수신 -> STOMP로 방 토픽 브로드캐스트
 * */
public record RoomEndedEvent(
        Long roomId
) {
}