package com.wanted.momocity.auth.application.service;

import com.wanted.momocity.auth.application.port.RefreshTokenRepositoryPort;
import com.wanted.momocity.auth.application.port.TokenProviderPort;
import com.wanted.momocity.auth.application.usecase.RefreshTokenUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RefreshService implements RefreshTokenUseCase {

    private final TokenProviderPort tokenProviderPort;
    private final RefreshTokenRepositoryPort refreshTokenRepositoryPort;

    @Override
    public String refreshAccessToken(String refreshToken) {
        tokenProviderPort.validateToken(refreshToken);
        Authentication authentication = tokenProviderPort.getAuthentication(refreshToken);
        // DB에서 저장된 리프레시 토큰이랑 비교
        // 새 액세스 토큰 발급
        return tokenProviderPort.createAccessToken(authentication);
    }
}
