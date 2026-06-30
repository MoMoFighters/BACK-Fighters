package com.wanted.momocity.message.domain.event;

public record LeaveChatRoomWebsocketPublishedEvent(
        Long roomId,
        Long userId
) {
}
