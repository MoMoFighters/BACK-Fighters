package com.wanted.momocity.payment.presentation.api.response;

import java.time.LocalDateTime;

public record PaymentVerifyResponse(
        LocalDateTime membershipUntil
) {
}
