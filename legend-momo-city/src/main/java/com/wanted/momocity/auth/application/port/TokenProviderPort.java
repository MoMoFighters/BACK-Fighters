package com.wanted.momocity.auth.application.port;

import org.springframework.security.core.Authentication;

public interface TokenProviderPort {
    String createAccessToken(Authentication authentication);
    String createRefreshToken();
    boolean validateToken(String token);
    Authentication getAuthentication(String token);
    String getEmailFromToken(String token);
    long getRefreshTokenValidityMilliseconds();


}
