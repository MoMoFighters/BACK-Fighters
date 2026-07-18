package com.wanted.momocity.payment.application.service;

import com.wanted.momocity.payment.application.command.WebhookCommand;
import com.wanted.momocity.payment.application.usecase.PaymentWebhookUseCase;
import com.wanted.momocity.payment.domain.exception.PaymentNotFoundException;
import com.wanted.momocity.payment.domain.exception.WebhookProcessingIncompleteException;
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
        switch (command.eventType()) {
            case "Transaction.Paid" -> handlePaid(command.paymentId());
            case "Transaction.Cancelled" -> handleCancelled(command.paymentId());
            default -> log.info("[webhook] 처리 대상이 아닌 이벤트 타입 - 무시 eventType={}", command.eventType());
        }
    }

    private void handlePaid(String paymentId) {
        Payment payment = paymentRepository.findByPaymentId(paymentId)
                .orElseThrow(() -> new PaymentNotFoundException(
                        "결제 정보를 찾을 수 없습니다. paymentId=" + paymentId));

        if (payment.isFinalized()) {
            log.info("[webhook] 이미 처리된 결제 건 - 스킵 paymentId={}", payment.getPaymentId());
            return;
        }
        paymentConfirmService.confirm(payment);
    }


    private void handleCancelled(String paymentId) {
        Payment payment = paymentRepository.findByPaymentId(paymentId)
                .orElseThrow(() -> new PaymentNotFoundException(
                        "결제 정보를 찾을 수 없습니다. paymentId=" + paymentId));

        log.warn("[webhook] 취소 이벤트 수신 - 수동 확인 필요 paymentId={}, currentStatus={}",
                payment.getPaymentId(), payment.getStatus());

        // 아직 실제 반영 로직이 없으므로, PortOne한테 "처리 실패"로 알려서
        // 재전송 정책(0→1→4→16→64→256분)에 따라 다시 시도하게 만든다.
        // 나중에 실제 반영 로직이 추가되면 이 예외는 제거해야 한다.
        throw new WebhookProcessingIncompleteException("취소 이벤트 수동 확인 대기 중");
    }
}
