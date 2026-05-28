package com.wanted.momocity.message.application.command;

public record LeaveChatRoomCommand(
        Long roomId,
        Long userId
) {
}
