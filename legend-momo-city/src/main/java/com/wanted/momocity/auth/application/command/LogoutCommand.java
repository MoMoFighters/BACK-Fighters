package com.wanted.momocity.auth.application.command;

public record LogoutCommand(
        String accessToken,
        String refreshToken
) {
}
