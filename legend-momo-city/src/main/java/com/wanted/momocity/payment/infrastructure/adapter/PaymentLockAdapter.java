package com.wanted.momocity.payment.infrastructure.adapter;

import com.wanted.momocity.payment.application.port.PaymentLockPort;
import com.wanted.momocity.payment.domain.model.Plan;
import lombok.RequiredArgsConstructor;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Component
@RequiredArgsConstructor
public class PaymentLockAdapter implements PaymentLockPort {

    private final RedissonClient redissonClient;
    /*comment
    *  RedissonClient : Redis로 분산 락/큐 같은 복잡한 기능을 만들 때 필요한 반복적인 코드를 미리 만들어둔 라이브러리
    *  - RLock : 분산 락
    *  redisTemplate.opsForValue().setIfAbsent(... 이런 식으로 쓰던 코드를 lock.tryLock(.. 이렇게 간결히 쓸 수 있다 !*/

    @Override
    public boolean tryLock(Long userId, Plan plan) {
        RLock lock = redissonClient.getLock("payment:lock:" + userId);
        try {
            // 대기 0초(바로 실패), 최대 5분간 락 유지
            return lock.tryLock(0, 300, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return false;
        }
    }

    @Override
    public void unlock(Long userId, Plan plan) {
        RLock lock = redissonClient.getLock("payment:lock:" + userId);
        if (lock.isHeldByCurrentThread()) {
            lock.unlock();
        }
    }
}
