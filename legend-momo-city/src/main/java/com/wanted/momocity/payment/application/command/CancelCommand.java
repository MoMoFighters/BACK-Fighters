package com.wanted.momocity.payment.application.command;

public record CancelCommand(
        Long userId,
        String paymentId
) {
}
