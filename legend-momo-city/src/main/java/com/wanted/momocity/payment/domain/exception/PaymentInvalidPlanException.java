package com.wanted.momocity.payment.domain.exception;

public class PaymentInvalidPlanException extends RuntimeException {
    public PaymentInvalidPlanException(String message) {
        super(message);
    }
}
