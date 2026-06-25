package com.wanted.momocity.message.domain.event;

public record LeftRoomPublishedEvent(
        Long roomId,
        Long userId, //채팅방 나간 사용자
        String userNickname //채팅방 나간 사용자의 닉네임
) {
}
