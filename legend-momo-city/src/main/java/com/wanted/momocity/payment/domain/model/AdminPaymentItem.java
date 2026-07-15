package com.wanted.momocity.payment.domain.model;

import java.time.LocalDateTime;

public record AdminPaymentItem(
        String userName,
        long price,
        Plan plan,
        Status status,
        LocalDateTime createdAt
) {
}
