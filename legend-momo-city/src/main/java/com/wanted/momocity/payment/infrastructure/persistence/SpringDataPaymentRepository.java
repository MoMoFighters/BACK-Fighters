package com.wanted.momocity.payment.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

public interface SpringDataPaymentRepository extends JpaRepository<PaymentJpaEntity, Long> {
}
