package com.wanted.momocity.payment.domain.exception;

public class PaymentAlreadyInProgressException extends RuntimeException {
    public PaymentAlreadyInProgressException(String message) {
        super(message);
    }
}
