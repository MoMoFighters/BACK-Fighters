package com.wanted.momocity.message.application.query;

public record GetMessageHistoryQuery(
        Long roomId,
        Long userId,
        Long lastMessageId
) {
}
