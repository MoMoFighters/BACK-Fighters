package com.wanted.momocity.auth.application.port;

import java.time.Instant;
import java.util.Optional;

public interface RefreshTokenRepositoryPort {
    void save(String email, String token, Instant expiryDate);
    Optional<String> findByEmail(String email);
    Optional<String> findByToken(String token);
    void deleteByEmail(String email);
}
