package com.wanted.momocity.user.application.command;

public record NicknameRegisterCommand(
        Long userId,
        String nickname
) {
}
