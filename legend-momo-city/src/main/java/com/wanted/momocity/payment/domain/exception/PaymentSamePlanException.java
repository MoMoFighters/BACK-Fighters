package com.wanted.momocity.payment.domain.exception;

public class PaymentSamePlanException extends RuntimeException {
    public PaymentSamePlanException(String message) {
        super(message);
    }
}
