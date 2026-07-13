package com.wanted.momocity.payment.application.policy;

import com.wanted.momocity.payment.domain.exception.PaymentAccessDeniedException;
import com.wanted.momocity.payment.domain.exception.PaymentRefundNotAllowedException;
import com.wanted.momocity.payment.domain.model.Payment;
import com.wanted.momocity.payment.domain.model.Status;
import com.wanted.momocity.payment.domain.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
public class CancelPolicy {

    private final PaymentRepository paymentRepository;
    private static final long REFUNDABLE_DAYS = 3;

    public void validateOwnership(Payment payment, Long userId) {
        if (!payment.getUserId().equals(userId)) {
            throw new PaymentAccessDeniedException("결제 소유자가 아닙니다.");
        }
    }

    // 결제 후 3일 이내에만 환불 가능
    public void checkRefundable(Payment payment) {
        boolean withinPeriod = payment.getStatus() == Status.SUCCESS
                && payment.getCreatedAt().plusDays(REFUNDABLE_DAYS).isAfter(LocalDateTime.now());

        // 이미 환불 한건지 아닌지 확인
        boolean alreadyRefunded = paymentRepository
                .existsByPaymentIdAndStatus(payment.getPaymentId(), Status.REFUND);

        if (!withinPeriod || alreadyRefunded) {
            throw new PaymentRefundNotAllowedException(
                    "환불 가능 기간이 지났거나 이미 환불된 결제 건입니다.");
        }
    }
}
