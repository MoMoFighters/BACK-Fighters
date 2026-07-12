package com.wanted.momocity.payment.application.service;

import com.wanted.momocity.payment.application.command.PaymentPrepareCommand;
import com.wanted.momocity.payment.application.policy.PaymentPolicy;
import com.wanted.momocity.payment.application.port.GetUserMembershipPort;
import com.wanted.momocity.payment.application.usecase.PaymentCommandUseCase;
import com.wanted.momocity.payment.domain.exception.PaymentAlreadyInProgressException;
import com.wanted.momocity.payment.domain.model.Payment;
import com.wanted.momocity.payment.domain.model.PaymentPrepareResult;
import com.wanted.momocity.payment.domain.model.Plan;
import com.wanted.momocity.payment.domain.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional
public class PaymentCommandService implements PaymentCommandUseCase {

    private final PaymentRepository paymentRepository;
    private final GetUserMembershipPort getUserMembershipPort;
    private final PaymentPolicy paymentPolicy;

    // 결제 준비 - 결제 금액 저장용
    @Override
    public PaymentPrepareResult paymentPrepare(PaymentPrepareCommand command) {

        Plan currentPlan = getUserMembershipPort.getCurrentPlan(command.userId());
        Plan targetPlan = command.plan();

        // 플랜 변경 유효성 검증 + 결제 금액 계산
        Long price = paymentPolicy.calculatePrice(currentPlan, targetPlan);

        // 중복 결제 방지
        paymentRepository.findPendingByUserIdAndPlan(command.userId(), targetPlan)
                .ifPresent(p -> {
                    throw new PaymentAlreadyInProgressException("이미 진행 중인 결제가 있습니다.");
                });

        String paymentId = UUID.randomUUID().toString();
        Payment payment = Payment.createPending(
                command.userId(), paymentId, command.plan(), price
        );

        Payment prepare = paymentRepository.save(payment);

        return new PaymentPrepareResult(prepare.getPrice(), prepare.getCreatedAt(), prepare.getPaymentId());
    }
}
