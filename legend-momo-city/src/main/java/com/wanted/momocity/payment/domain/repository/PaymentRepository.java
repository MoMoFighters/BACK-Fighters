package com.wanted.momocity.payment.domain.repository;

import com.wanted.momocity.payment.domain.model.Payment;
import com.wanted.momocity.payment.domain.model.Plan;

import java.util.Optional;

public interface PaymentRepository {

    // 결제 정보 저장 - prepare
    Payment save(Payment payment);

    // 중복 결제 방지용 이미 있으면 예외
    Optional<Payment> findPendingByUserIdAndPlan(Long userId, Plan targetPlan);
}
