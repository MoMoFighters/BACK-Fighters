package com.wanted.momocity.message.application.command;

public record GetMessageHistoryQuery(
        Long roomId,
        Long userId,
        Long lastMessageId
) {
}
