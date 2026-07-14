package com.wanted.momocity.payment.domain.model;

import java.time.LocalDateTime;

public record PaymentPrepareResult(
        Long price,
        LocalDateTime createdAt,
        String paymentId
) {
}
