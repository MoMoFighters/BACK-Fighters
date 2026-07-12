package com.wanted.momocity.payment.domain.repository;

import com.wanted.momocity.payment.domain.model.Payment;

public interface PaymentRepository {

    // 결제 정보 저장 - prepare
    Payment save(Payment payment);
}
