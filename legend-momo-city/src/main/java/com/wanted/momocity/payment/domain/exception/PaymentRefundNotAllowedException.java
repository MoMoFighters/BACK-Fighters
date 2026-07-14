package com.wanted.momocity.payment.domain.exception;

public class PaymentRefundNotAllowedException extends RuntimeException {
    public PaymentRefundNotAllowedException(String message) {
        super(message);
    }
}
