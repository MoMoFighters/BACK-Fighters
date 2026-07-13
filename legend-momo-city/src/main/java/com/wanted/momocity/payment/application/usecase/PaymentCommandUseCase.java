package com.wanted.momocity.payment.application.usecase;

import com.wanted.momocity.payment.application.command.PaymentPrepareCommand;
import com.wanted.momocity.payment.application.command.PaymentVerifyCommand;
import com.wanted.momocity.payment.domain.model.PaymentPrepareResult;
import com.wanted.momocity.payment.domain.model.PaymentVerifyResult;

public interface PaymentCommandUseCase {
    // 결제 준비 - 결제 금액 저장용
    PaymentPrepareResult paymentPrepare(PaymentPrepareCommand paymentPrepareCommand);

    // 결제 검증
    PaymentVerifyResult paymentVerify(PaymentVerifyCommand paymentVerifyCommand);
}
