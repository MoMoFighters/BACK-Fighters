package com.wanted.momocity.auth.application.usecase;

public interface NewTokenUsecase {

    String refreshAccessToken(String refreshToken, String oldAccessToken);
}
