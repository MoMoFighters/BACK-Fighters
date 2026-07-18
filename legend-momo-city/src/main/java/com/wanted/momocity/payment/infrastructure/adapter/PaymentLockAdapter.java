package com.wanted.momocity.payment.infrastructure.adapter;

import com.wanted.momocity.payment.application.port.PaymentLockPort;
import com.wanted.momocity.payment.domain.model.Plan;
import lombok.RequiredArgsConstructor;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
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
            // leaseTime을 안 주면 Redisson watchdog이 기본 30초 TTL로 락을 잡고
            // 락이 살아있는 동안 10초마다 자동으로 TTL을 연장
            // 그래서 처리 시간이 얼마나 걸리든 (unlock() 호출 전까지) 락이 중간에 안 풀림
            return lock.tryLock(0, TimeUnit.SECONDS);
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
