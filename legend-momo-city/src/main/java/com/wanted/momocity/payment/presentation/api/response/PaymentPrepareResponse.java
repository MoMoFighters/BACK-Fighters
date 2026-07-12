package com.wanted.momocity.payment.presentation.api.response;

import java.time.LocalDateTime;

public record PaymentPrepareResponse(
        Long price,
        LocalDateTime createdAt,
        String paymentId) {
}
