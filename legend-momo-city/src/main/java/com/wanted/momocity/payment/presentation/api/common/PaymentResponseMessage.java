package com.wanted.momocity.payment.presentation.api.common;

public final class PaymentResponseMessage {

    private PaymentResponseMessage(){}

    public static final String PAYMENT_READY = "결제 준비 완료";
    public static final String PAYMENT_VERIFIED = "결제 검증 완료";
    public static final String SUBSCRIBE_CANCEL = "환불처리 되었습니다. 멤버십이 BASIC으로 전환됩니다.";
    public static final String FETCH_SUCCESS = "조회 성공";

}
