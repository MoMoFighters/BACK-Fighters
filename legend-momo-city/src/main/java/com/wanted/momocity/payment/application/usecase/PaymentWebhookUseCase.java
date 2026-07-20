package com.wanted.momocity.payment.application.usecase;

import com.wanted.momocity.payment.application.command.WebhookCommand;

public interface PaymentWebhookUseCase {
    void handle(WebhookCommand command);

}
