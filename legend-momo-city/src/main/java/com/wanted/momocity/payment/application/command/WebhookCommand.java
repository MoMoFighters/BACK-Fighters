package com.wanted.momocity.payment.application.command;

public record WebhookCommand(
        String eventType,
        String paymentId
) {
}
