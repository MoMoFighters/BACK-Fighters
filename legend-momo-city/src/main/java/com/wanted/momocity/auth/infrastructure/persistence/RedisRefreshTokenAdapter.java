package com.wanted.momocity.auth.infrastructure.persistence;

import com.wanted.momocity.auth.application.port.RedisRefreshTokenPort;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

@RequiredArgsConstructor
@Component
public class RedisRefreshTokenAdapter implements RedisRefreshTokenPort {

    private final StringRedisTemplate redisTemplate;

    // redis 키 네이밍 컨벤션 (도메인:타입:값)
    private static final String TOKEN_PREFIX = "refresh:token:";
    private static final String USER_PREFIX = "refresh:user:";


    @Override
    public void save(String userId, String token, Instant expiryDate) {
        long ttl = expiryDate.toEpochMilli() - Instant.now().toEpochMilli();

        // 기존 토큰 삭제
        deleteByUserId(userId);

        // redis에 userid 저장
        redisTemplate.opsForValue()
                .set(TOKEN_PREFIX + token, userId, ttl, TimeUnit.MILLISECONDS);

        // redis에 토큰값 저장
        redisTemplate.opsForValue()
                .set(USER_PREFIX + userId, token, ttl, TimeUnit.MILLISECONDS);
    }

    @Override
    public Optional<String> findByUserId(String userId) {
        return Optional.ofNullable(
                redisTemplate.opsForValue().get(USER_PREFIX + userId)
        );
    }

    @Override
    public Optional<String> findByToken(String token) {
        String userId = redisTemplate.opsForValue().get(TOKEN_PREFIX + token);
        return Optional.ofNullable(userId).map(u -> token);
    }

    @Override
    public void deleteByUserId(String userId) {
        String token = redisTemplate.opsForValue().get(USER_PREFIX + userId);
        if (token != null) {
            redisTemplate.delete(TOKEN_PREFIX + token);
        }
        redisTemplate.delete(USER_PREFIX + userId);
    }

    @Override
    public void deleteByToken(String token) {
        String userId = redisTemplate.opsForValue().get(TOKEN_PREFIX + token);
        if (userId != null) {
            redisTemplate.delete(USER_PREFIX + userId);
        }
        redisTemplate.delete(TOKEN_PREFIX + token);
    }
}