package com.wanted.momocity.auth.infrastructure.email;

import com.wanted.momocity.auth.application.port.EmailCodePort;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Component
@RequiredArgsConstructor
public class EmailCodeAdapter implements EmailCodePort {

    private final StringRedisTemplate redisTemplate;

    @Override
    public void save(String email, String code, long ttlSeconds) {
        redisTemplate.opsForValue().set(email, code, ttlSeconds, TimeUnit.SECONDS);
    }

    @Override
    public String find(String email) {
        return redisTemplate.opsForValue().get(email);
    }

    @Override
    public void delete(String email) {
        redisTemplate.delete(email);
    }
}
