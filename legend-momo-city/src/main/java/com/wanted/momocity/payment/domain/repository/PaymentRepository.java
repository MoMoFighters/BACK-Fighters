package com.wanted.momocity.payment.domain.repository;

import com.wanted.momocity.payment.domain.model.Payment;

import java.util.Optional;

public interface PaymentRepository {

    // 결제 정보 저장 - prepare
    Payment save(Payment payment);

    // 실제 결제 정보 조회
    Optional<Payment> findByPaymentId(String paymentId);
}
