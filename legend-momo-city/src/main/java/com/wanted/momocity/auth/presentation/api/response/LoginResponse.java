package com.wanted.momocity.auth.presentation.api.response;

public record LoginResponse(
        String accessToken,
        String refreshToken
) {



}
