package com.wanted.momocity.payment.domain.model;

import java.time.LocalDateTime;

public record PersonalPaymentItem(
        String paymentId,
        long price,
        Plan plan,
        Status status,
        LocalDateTime createdAt
) {
}
