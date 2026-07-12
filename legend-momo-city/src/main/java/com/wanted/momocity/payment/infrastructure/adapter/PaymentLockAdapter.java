package com.wanted.momocity.payment.infrastructure.adapter;

import com.wanted.momocity.payment.application.port.PaymentLockPort;
import com.wanted.momocity.payment.domain.model.Plan;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Component
@RequiredArgsConstructor
public class PaymentLockAdapter implements PaymentLockPort {

    private final StringRedisTemplate redisTemplate;
    private static final long LOCK_TTL_SECONDS = 300L; //5분

    @Override
    public boolean tryLock(Long userId, Plan plan) {
        String key = "payment:lock:" + userId ;
        return Boolean.TRUE.equals(
                redisTemplate.opsForValue().setIfAbsent(key, "locked", LOCK_TTL_SECONDS, TimeUnit.SECONDS)
        );
    }

    @Override
    public void unlock(Long userId, Plan plan) {
        String key = "payment:lock:" + userId ;
        redisTemplate.delete(key);
    }
}
