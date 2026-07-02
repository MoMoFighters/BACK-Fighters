package com.wanted.momocity.message.domain.event;

import java.util.List;

public record ChatRoomLeaveWebsocketPublishedEvent(
        Long roomId,
        Long leaveUserId,
        List<Long> receiverIds
) {
}
