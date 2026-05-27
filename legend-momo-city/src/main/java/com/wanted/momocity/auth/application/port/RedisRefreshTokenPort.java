package com.wanted.momocity.auth.application.port;

import java.time.Instant;
import java.util.Optional;

public interface RedisRefreshTokenPort {
    void save(String userId, String token, Instant expiryDate);
    Optional<String> findByUserId(String userId);
    Optional<String> findByToken(String token);
    void deleteByUserId(String userId);

    // 로그아웃용
    void deleteByToken(String token);

}
