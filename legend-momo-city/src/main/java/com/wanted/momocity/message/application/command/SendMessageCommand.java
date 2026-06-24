package com.wanted.momocity.message.application.command;

public record SendMessageCommand(
        Long roomId,
        Long senderId,
        String content
) {
}
