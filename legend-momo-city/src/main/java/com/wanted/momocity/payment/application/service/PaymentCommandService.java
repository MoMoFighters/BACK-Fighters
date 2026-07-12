package com.wanted.momocity.payment.application.service;

import com.wanted.momocity.payment.application.command.PaymentPrepareCommand;
import com.wanted.momocity.payment.application.usecase.PaymentCommandUseCase;
import com.wanted.momocity.payment.domain.model.Payment;
import com.wanted.momocity.payment.domain.model.PaymentPrepareResult;
import com.wanted.momocity.payment.domain.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional
public class PaymentCommandService implements PaymentCommandUseCase {

    private final PaymentRepository paymentRepository;

    // 결제 준비 - 결제 금액 저장용
    @Override
    public PaymentPrepareResult paymentPrepare(PaymentPrepareCommand command) {
        String paymentId = UUID.randomUUID().toString();

        Payment payment = Payment.createPending(
                command.userId(), paymentId, command.plan(), command.price()
        );

        Payment prepare = paymentRepository.save(payment);

        return new PaymentPrepareResult(prepare.getPrice(), prepare.getCreatedAt(), prepare.getPaymentId());
    }
}
