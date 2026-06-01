package com.wanted.momocity.auth.presentation.api.response;

import com.wanted.momocity.auth.domain.model.Status;

public record LoginResponse(
        String accessToken,
        String refreshToken,
        Status status,
        long expiresIn
) {



}
