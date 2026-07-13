package com.wanted.momocity.payment.domain.exception;

public class PaymentAlreadyVerifiedException extends RuntimeException {
    public PaymentAlreadyVerifiedException(String message) {
        super(message);
    }
}
