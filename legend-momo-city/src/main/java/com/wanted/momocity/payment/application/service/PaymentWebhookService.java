package com.wanted.momocity.payment.application.service;

import com.wanted.momocity.payment.application.command.WebhookCommand;
import com.wanted.momocity.payment.application.usecase.PaymentWebhookUseCase;
import com.wanted.momocity.payment.domain.exception.PaymentNotFoundException;
import com.wanted.momocity.payment.domain.model.Payment;
import com.wanted.momocity.payment.domain.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional
public class PaymentWebhookService implements PaymentWebhookUseCase {

    private final PaymentRepository paymentRepository;
    private final PaymentConfirmService paymentConfirmService;

    @Override
    public void handle(WebhookCommand command) {
        Payment payment = paymentRepository.findByPaymentId(command.paymentId())
                .orElseThrow(() -> new PaymentNotFoundException(
                        "결제 정보를 찾을 수 없습니다. paymentId=" + command.paymentId()));

        switch (command.eventType()) {
            case "Transaction.Paid" -> handlePaid(payment);
            case "Transaction.Cancelled" -> handleCancelled(payment);
            default -> log.info("[webhook] 처리 대상이 아닌 이벤트 타입 - 무시 eventType={}", command.eventType());
        }
    }

    private void handlePaid(Payment payment) {
        if (payment.isFinalized()) {
            log.info("[webhook] 이미 처리된 결제 건 - 스킵 paymentId={}", payment.getPaymentId());
            return;
        }
        paymentConfirmService.confirm(payment);
    }

    private void handleCancelled(Payment payment) {
        log.warn("[webhook] 취소 이벤트 수신 - 수동 확인 필요 paymentId={}, currentStatus={}",
                payment.getPaymentId(), payment.getStatus());
    }
}
