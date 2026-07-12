package com.wanted.momocity.payment.application.usecase;

import com.wanted.momocity.payment.application.command.PaymentPrepareCommand;
import com.wanted.momocity.payment.domain.model.PaymentPrepareResult;

public interface PaymentCommandUseCase {
    // 결제 준비 - 결제 금액 저장용
    PaymentPrepareResult paymentPrepare(PaymentPrepareCommand paymentPrepareCommand);
}
