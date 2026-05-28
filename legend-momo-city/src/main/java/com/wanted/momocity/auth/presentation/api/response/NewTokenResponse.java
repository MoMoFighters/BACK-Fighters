package com.wanted.momocity.auth.presentation.api.response;

public record NewTokenResponse(
        String accessToken,
        long expiresIn
) {
}
