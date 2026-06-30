package com.wanted.momocity.message.domain.event;

import java.util.List;

public record ChatRoomRenamedWebsocketPublishedEvent(
        Long roomId,
        List<Long> memberIds
) {
}
