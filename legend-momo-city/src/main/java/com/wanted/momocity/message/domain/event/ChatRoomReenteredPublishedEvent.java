package com.wanted.momocity.message.domain.event;

import java.util.List;

public record ChatRoomReenteredPublishedEvent(
        Long roomId,
        List<Long> memberIds
) {
}
