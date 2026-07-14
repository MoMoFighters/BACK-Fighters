package com.wanted.momocity.payment.domain.exception;

public class PaymentDowngradeNotAllowedException extends RuntimeException {
    public PaymentDowngradeNotAllowedException(String message) {
        super(message);
    }
}
