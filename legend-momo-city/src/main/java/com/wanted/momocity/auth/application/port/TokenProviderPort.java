package com.wanted.momocity.auth.application.port;

import org.springframework.security.core.Authentication;

public interface TokenProviderPort {
    String createAccessToken(Authentication authentication);
    String createRefreshToken(String userId);

    boolean validateToken(String token);
    Authentication getAuthentication(String token);

    String getIdFromToken(String token);

    long getRefreshTokenValidityMilliseconds();
    long getAccessTokenValidityMilliseconds();

    // 소셜 로그인 용 -> 이메일이랑 role만 가지고 access 토큰 발급하기
    String createAccessToken(String userId, String role);


}
