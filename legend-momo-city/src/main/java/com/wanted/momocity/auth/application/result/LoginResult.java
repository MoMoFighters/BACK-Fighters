package com.wanted.momocity.auth.application.result;

public record LoginResult(
        String accessToken,
        String refreshToken,
        long expiresIn
) {
}
