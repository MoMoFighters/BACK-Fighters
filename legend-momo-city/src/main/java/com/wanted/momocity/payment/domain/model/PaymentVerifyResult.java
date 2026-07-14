package com.wanted.momocity.payment.domain.model;

import java.time.LocalDateTime;

public record PaymentVerifyResult(
        String paymentId,
        Status status,
        LocalDateTime membershipUntil
) {
}
