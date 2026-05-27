package com.wanted.momocity.auth.application.port;

import java.time.Instant;
import java.util.Optional;

public interface RefreshTokenRepositoryPort {
    void save(String userId, String token, Instant expiryDate);
    Optional<String> findByUserId(String userId);
    Optional<String> findByToken(String token);
    void deleteByEmail(String email);
}
