package com.wanted.momocity.auth.infrastructure.persistence;

import com.wanted.momocity.auth.application.port.RefreshTokenRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Optional;

@RequiredArgsConstructor
@Component
public class RefreshTokenRepositoryAdapter implements RefreshTokenRepositoryPort{

    private final SpringDataRefreshTokenRepository springDataRefreshTokenRepository;

//    @Override
//    public void save(String username, String token, Instant expiryDate) {
//        springDataRefreshTokenRepository.deleteByUsername(username); // 기존 토큰 삭제
//        springDataRefreshTokenRepository.save(new RefreshTokenJpaEntity(username, token, expiryDate));
//    }

    @Override
    public void save(String email, String token, Instant expiryDate) {
        springDataRefreshTokenRepository.deleteByEmail(email); // 기존 토큰 삭제
        springDataRefreshTokenRepository.save(new RefreshTokenJpaEntity(email, token, expiryDate));
    }

    @Override
    public Optional<String> findByUserId(String userId) {
        return springDataRefreshTokenRepository.findByEmail(userId)
                .map(RefreshTokenJpaEntity::getToken);
    }

    @Override
    public Optional<String> findByToken(String token) {
        return springDataRefreshTokenRepository.findByToken(token)
                .map(RefreshTokenJpaEntity::getToken);
    }

    @Override
    public void deleteByEmail(String email) {
        springDataRefreshTokenRepository.deleteByEmail(email);
    }
}
