package com.wanted.momocity.user.application.command;

public record UpdateUserInfoCommand(
        Long userId,
        String profileImageUrl,
        String nickname,
        String currentPassword,
        String password
) {}

