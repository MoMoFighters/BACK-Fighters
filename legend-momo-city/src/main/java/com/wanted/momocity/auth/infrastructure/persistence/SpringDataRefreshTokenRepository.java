package com.wanted.momocity.auth.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SpringDataRefreshTokenRepository extends JpaRepository<RefreshTokenJpaEntity, Long> {
    Optional<RefreshTokenJpaEntity> findByToken(String token);
    Optional<RefreshTokenJpaEntity> findByEmail(String email);
    void deleteByEmail(String email);
}
