package com.wanted.momocity.payment.domain.exception;

public class WebhookProcessingIncompleteException extends RuntimeException {
    public WebhookProcessingIncompleteException(String message) {
        super(message);
    }
}
