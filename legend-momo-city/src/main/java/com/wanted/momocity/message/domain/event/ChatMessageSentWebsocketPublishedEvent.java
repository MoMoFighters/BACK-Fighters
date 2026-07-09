package com.wanted.momocity.message.domain.event;

import java.util.List;

public record ChatMessageSentWebsocketPublishedEvent(
        Long roomId,
        Long senderId,
        List<Long> receiverIds
) {
}
