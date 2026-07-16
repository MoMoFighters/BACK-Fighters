package com.wanted.momocity.payment.application.service;

import com.wanted.momocity.payment.application.command.CancelCommand;
import com.wanted.momocity.payment.application.command.PaymentPrepareCommand;
import com.wanted.momocity.payment.application.command.PaymentVerifyCommand;
import com.wanted.momocity.payment.application.policy.CancelPolicy;
import com.wanted.momocity.payment.application.policy.PaymentPolicy;
import com.wanted.momocity.payment.application.port.GetUserMembershipPort;
import com.wanted.momocity.payment.application.port.PaymentLockPort;
import com.wanted.momocity.payment.application.port.PortOnePaymentPort;
import com.wanted.momocity.payment.application.port.SetUserMembershipPort;
import com.wanted.momocity.payment.application.supporter.PaymentStatusUpdater;
import com.wanted.momocity.payment.application.usecase.PaymentCommandUseCase;
import com.wanted.momocity.payment.domain.exception.*;
import com.wanted.momocity.payment.domain.model.*;
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
    private final GetUserMembershipPort getUserMembershipPort;
    private final SetUserMembershipPort setUserMembershipPort;
    private final PaymentPolicy paymentPolicy;
    private final PortOnePaymentPort portOnePaymentPort;
    private final PaymentLockPort paymentLockPort;
    private final PaymentStatusUpdater paymentStatusUpdater;
    private final CancelPolicy cancelPolicy;
    private final PaymentConfirmService paymentConfirmService;

    // 결제 준비 - 결제 금액 저장용
    @Override
    public PaymentPrepareResult paymentPrepare(PaymentPrepareCommand command) {

        // 플랜 변경 유효성 검증 + 결제 금액 계산
        GetUserMembershipPort.UserMembership membership = getUserMembershipPort.getUserMembership(command.userId());
        Long price = paymentPolicy.calculatePrice(membership.plan(), command.plan());

        // 중복 결제 방지
        if (!paymentLockPort.tryLock(command.userId(), command.plan())) {
            throw new PaymentAlreadyInProgressException("이미 진행 중인 결제가 있습니다.");
        }

        try {
            String paymentId = UUID.randomUUID().toString();
            Payment payment = Payment.createPending(command.userId(), paymentId, null,command.plan(), price);
            Payment prepare = paymentRepository.save(payment);
            return new PaymentPrepareResult(prepare.getPrice(), prepare.getCreatedAt(), prepare.getPaymentId());

        } catch (Exception e) {
            paymentLockPort.unlock(command.userId(), command.plan());
            throw e;
        }
    }

    /*comment
    *  1. 이 결제 건이 진짜 이 사람 건이 맞는지
    *  2. 이미 처리 끝난 건 아닌지
    *  이 두가 지에 대한 검증만 하고 실제 db에 적용하고 처리하는 건 confirmservice가 함 */
    @Override
    public PaymentVerifyResult paymentVerify(PaymentVerifyCommand command) {
        // 결제 건 조회
        Payment payment = paymentRepository.findByPaymentId(command.paymentId())
                .orElseThrow(() -> new PaymentNotFoundException("결제 정보를 찾을 수 없습니다."));

        // 정말 본인이 맞는지 확인
        if (!payment.getUserId().equals(command.userId())) {
            throw new PaymentAccessDeniedException("결제 소유자가 아닙니다.");
        }

        // 이미 웹훅이 처리한 경우 -> 그 결과 그대로 응답
        if (payment.isFinalized()) {
            return toVerifyResult(payment);
        }

        try {
            return paymentConfirmService.confirm(payment);
        } catch (PaymentAlreadyInProgressException e) {
            // 웹훅이 처리 중인 상황 -> 기다렸다가 결과를 다시 확인
            return waitAndFetchResult(command.paymentId());
        }
    }


     // 이미 확정된 결제 건을 응답 형태로 변환
    private PaymentVerifyResult toVerifyResult(Payment payment) {
        GetUserMembershipPort.UserMembership membership =
                getUserMembershipPort.getUserMembership(payment.getUserId());
        LocalDateTime membershipUntil = membership.membershipStart().plusDays(30);
        return new PaymentVerifyResult(payment.getPaymentId(), payment.getStatus(), membershipUntil);
    }

    /*comment
     * 다른 요청이 이미 락을 잡고 처리 중일 때 그 처리가 끝날 때까지 기다렸다가
     * 상태를 다시 읽어 오고 정해진 횟수 안에 끝나지 않으면 그때는 진짜로
     * 409
     */
    private PaymentVerifyResult waitAndFetchResult(String paymentId) {
        for (int i = 0; i < 5; i++) {
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }

            Payment refreshed = paymentRepository.findByPaymentId(paymentId)
                    .orElseThrow(() -> new PaymentNotFoundException("결제 정보를 찾을 수 없습니다."));

            if (refreshed.isFinalized()) {
                return toVerifyResult(refreshed);
            }
        }
        throw new PaymentAlreadyInProgressException("결제 처리 중입니다. 잠시 후 다시 확인해주세요.");
    }

    @Override
    public void cancel(CancelCommand command) {
        Payment payment = paymentRepository.findByPaymentId(command.paymentId())
                .orElseThrow(() -> new PaymentNotFoundException("결제 정보를 찾을 수 없습니다."));

        // 본인이 결제한 게 아니거나 결제 후 3일이 넘게 지나면 환불 불가능
        cancelPolicy.validateOwnership(payment, command.userId());
        cancelPolicy.checkRefundable(payment);

        // 동시 취소 요청 방지
        if (!paymentLockPort.tryLock(command.userId(), payment.getPlan())) {
            throw new PaymentAlreadyInProgressException("이미 처리 중인 요청이 있습니다.");
        }

        try {
            portOnePaymentPort.cancelPayment(command.paymentId(), "사용자 요청에 의한 환불");

            String refundPaymentId = UUID.randomUUID().toString();
            Payment refund = Payment.createRefund(payment, refundPaymentId);
            paymentRepository.save(refund);

            setUserMembershipPort.updateMembership(command.userId(), Plan.BASIC, LocalDateTime.now());

        } catch (Exception e) {
            log.error("[cancel] 환불 처리 실패 paymentId={}, userId={}",
                    command.paymentId(), command.userId(), e);
            String failedPaymentId = UUID.randomUUID().toString();
            Payment cancelFailed = Payment.createCancelFailed(payment, failedPaymentId);
            paymentStatusUpdater.saveCancelFailed(cancelFailed);
            throw new PaymentCancelFailedException("환불 취소 처리 실패 paymentId=" + command.paymentId());
        } finally {
            paymentLockPort.unlock(command.userId(), payment.getPlan());
        }
    }
}
