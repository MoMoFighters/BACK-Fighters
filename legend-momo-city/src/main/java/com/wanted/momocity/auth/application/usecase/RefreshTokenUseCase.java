package com.wanted.momocity.auth.application.usecase;

public interface RefreshTokenUseCase {
    String refreshAccessToken(String refreshToken);
}
