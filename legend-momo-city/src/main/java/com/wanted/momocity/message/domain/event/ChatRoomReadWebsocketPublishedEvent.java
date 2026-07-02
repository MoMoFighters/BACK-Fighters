package com.wanted.momocity.message.domain.event;

import java.util.List;

public record ChatRoomReadWebsocketPublishedEvent(
        Long roomId,
        Long readerId,
        List<Long> memberIds
) {
}
