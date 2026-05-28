package com.wanted.momocity.message.domain.event;

import java.time.LocalDateTime;

public record SendMessagePublishedEvent(
        Long messageId,
        Long senderId,
        String senderNickname,
        LocalDateTime createdAt
) {
}
