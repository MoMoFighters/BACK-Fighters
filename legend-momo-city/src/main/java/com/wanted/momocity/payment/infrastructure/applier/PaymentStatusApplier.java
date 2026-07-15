package com.wanted.momocity.payment.infrastructure.applier;

import com.wanted.momocity.payment.application.port.PaymentLockPort;
import com.wanted.momocity.payment.application.port.SetUserMembershipPort;
import com.wanted.momocity.payment.domain.model.Payment;
import com.wanted.momocity.payment.infrastructure.persistence.PaymentJpaEntity;
import com.wanted.momocity.payment.infrastructure.persistence.SpringDataPaymentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
public class PaymentStatusApplier {
    private final SpringDataPaymentRepository springDataPaymentRepository;
    private final SetUserMembershipPort setUserMembershipPort;
    private final PaymentLockPort paymentLockPort;

    @Transactional
    public void applySuccess(Payment payment, LocalDateTime newMembershipStart) {
        Payment result = payment.markSuccess();
        setUserMembershipPort.updateMembership(payment.getUserId(), payment.getPlan(), newMembershipStart);
        springDataPaymentRepository.save(PaymentJpaEntity.fromDomain(result));
        paymentLockPort.unlock(payment.getUserId(), payment.getPlan());
    }

    @Transactional
    public void applyFailed(Payment payment) {
        Payment failed = payment.markFailed();
        springDataPaymentRepository.save(PaymentJpaEntity.fromDomain(failed));
        paymentLockPort.unlock(payment.getUserId(), payment.getPlan());
    }
}
