package com.wanted.momocity.message.application.command;

public record SendMessageCommand(
        Long senderId,
        Long roomId,
        String content
) {
}
