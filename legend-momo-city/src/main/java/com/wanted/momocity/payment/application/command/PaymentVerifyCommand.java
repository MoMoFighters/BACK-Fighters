package com.wanted.momocity.payment.application.command;

public record PaymentVerifyCommand(
        Long userId,
        String paymentId
) {
}
