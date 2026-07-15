package com.wanted.momocity.payment.domain.exception;

public class PaymentCancelFailedException extends RuntimeException {
    public PaymentCancelFailedException(String message) {
        super(message);
    }
}
