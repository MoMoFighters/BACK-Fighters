package com.wanted.momocity.message.application.command;

public record ReadMessageCommand(
        boolean isExisting, //기존 방 존재 여부
        Long roomId,
        Long targetUserId,
        String nickname,
        String role,
        String status
) {
}
