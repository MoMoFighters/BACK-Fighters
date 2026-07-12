package com.wanted.momocity.payment.infrastructure.persistence;

import com.wanted.momocity.payment.domain.model.Plan;
import com.wanted.momocity.payment.domain.model.Status;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SpringDataPaymentRepository extends JpaRepository<PaymentJpaEntity, Long> {

    Optional<PaymentJpaEntity> findFirstByUserIdAndPlanAndStatus(Long userId, Plan plan, Status status);
}
