package com.wanted.momocity.payment.application.port;

import com.wanted.momocity.payment.domain.model.PortOnePaymentDetail;

public interface PortOnePaymentPort {
    PortOnePaymentDetail verifyPayment(String paymentId);
    void cancelPayment(String paymentId, String reason);

}
