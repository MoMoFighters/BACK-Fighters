package com.wanted.momocity.payment.domain.exception;

public class PaymentNotAttemptedException extends RuntimeException {
    public PaymentNotAttemptedException(String message) {
        super(message);
    }
}
