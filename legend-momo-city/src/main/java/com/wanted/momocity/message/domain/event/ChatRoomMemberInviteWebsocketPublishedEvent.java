package com.wanted.momocity.message.domain.event;

import java.util.List;

public record ChatRoomMemberInviteWebsocketPublishedEvent(
        Long roomId,
        List<Long> targetMemberIds
) {
}
