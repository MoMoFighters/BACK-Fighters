package com.wanted.momocity.message.application.command;

public record ModifyRoomTitleCommand(
        Long roomId,
        Long userId,
        String roomTitle
) {
}
