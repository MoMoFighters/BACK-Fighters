package com.wanted.momocity.payment.application.port;

import com.wanted.momocity.payment.domain.model.Plan;

public interface PaymentLockPort {
    boolean tryLock(Long userId, Plan plan);
    void unlock(Long userId, Plan plan);
}
