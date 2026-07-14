package com.wanted.momocity.payment.domain.model;

public record PortOnePaymentDetail(
        String paymentId,
        String status,      // PortOne 응답의 status: PAID, FAILED, CANCELLED 등
        Long amount,         // 실결제금액
        String pgTxId       // 결제 대행사에서 발급하는 고유 번호
) {

    public boolean isPaid() {
        return "PAID".equals(status);
    }
}
