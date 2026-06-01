package com.wanted.momocity.auth.application.port;

import org.springframework.security.core.Authentication;

public interface TokenProviderPort {
    // 완전 새로운 액세스 토큰이랑 리프레시 토큰 만들기
    String createAccessToken(Authentication authentication);
    String createRefreshToken(String userId);

    boolean validateToken(String token);
    Authentication getAuthentication(String token);

    String getIdFromToken(String token);

    long getRefreshTokenValidityMilliseconds();
    long getAccessTokenValidityMilliseconds();

    // 임시비밀번호 발급용 3분짜리 액세스 토큰 생성
    String createTempAccessToken(Authentication authentication);

    // 소셜 로그인 용 -> 이메일이랑 role만 가지고 access 토큰 발급하기
    String createAccessToken(String userId, String role);

    // 액세스 토큰 블랙리스트 처리용 액세스 토큰 로그아웃 후 잔여시간 계산
    long getRemainingMillis(String accessToken);


}
