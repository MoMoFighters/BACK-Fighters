package com.wanted.momocity.payment.presentation.api.common;

public final class PaymentResponseMessage {

    private PaymentResponseMessage(){}

    public static final String PAYMENT_READY = "결제 준비 완료";
    public static final String PAYMENT_VERIFIED = "결제 검증 완료";
    public static final String SUBSCRIBE_CANCEL = "BASIC플랜으로 변경되었습니다.";
}
