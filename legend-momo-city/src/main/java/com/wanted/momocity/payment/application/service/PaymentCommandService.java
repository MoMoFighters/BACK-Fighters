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

    // 결제 검증
    @Override
    public PaymentVerifyResult paymentVerify(PaymentVerifyCommand command) {
        // 결제 건 조회
        Payment payment = paymentRepository.findByPaymentId(command.paymentId())
                .orElseThrow(() -> new PaymentNotFoundException("결제 정보를 찾을 수 없습니다."));

        // 정말 본인이 맞는지 확인
        if (!payment.getUserId().equals(command.userId())) {
            throw new PaymentAccessDeniedException("결제 소유자가 아닙니다.");
        }

        // 이미 검증 처리가 완료된 결제건
        if (payment.isFinalized()) {
            throw new PaymentAlreadyVerifiedException(
                    "이미 처리된 결제 건입니다. status=" + payment.getStatus()
            );
        }

        // 포트원에서 실제 결제 내역 조회
        PortOnePaymentDetail detail;
        try {
            detail = portOnePaymentPort.verifyPayment(command.paymentId());
        } catch (PortOneApiException e) {
            log.error("[verify] 포트원 API 호출 실패 paymentId={}, userId={}",
                    command.paymentId(), command.userId(), e);

            throw e;
        }

        // 검증
        /*comment
         *  1.포트원 응답 상태가 실제로 "결제완료(PAID)"인지
         *  2. 우리가 /prepare 때 저장해둔 가격과 포트원이 실제로 받은 금액이 정확히 일치하는지 */
        boolean amountMatches = detail.isPaid() && payment.getPrice().equals(detail.amount());

        // 검증 성송
        if (amountMatches) {
            Payment result = payment.markSuccess();

            // 현재 멤버십 시작일 조회 - 갱신 결제 시 기존 종료일 기준으로 연장
            GetUserMembershipPort.UserMembership membership = getUserMembershipPort.getUserMembership(command.userId());

            // 현재 멤버십 종료일 계산 (membershipStart + 30일)
            LocalDateTime currentUntil = membership.membershipStart().plusDays(30);

            // BASIC이면 오늘부터 시작
            // 유료 플랜이고 기간 남아있으면 기존 종료일부터 연장
            LocalDateTime newMembershipStart = (membership.plan() != Plan.BASIC && currentUntil.isAfter(LocalDateTime.now()))
                    ? currentUntil
                    : LocalDateTime.now();

            setUserMembershipPort.updateMembership(command.userId(), payment.getPlan(), newMembershipStart);
            LocalDateTime newMembershipUntil  = newMembershipStart.plusDays(30);

            paymentRepository.save(result);
            paymentLockPort.unlock(command.userId(), payment.getPlan()); // 여기서 캐시 제거
            return new PaymentVerifyResult(result.getPaymentId(), result.getStatus(), newMembershipUntil );
        }

        // 금액 불일치 - 여기서부터는 전부 실패
        if (detail.isPaid()) {
            try {
                portOnePaymentPort.cancelPayment(command.paymentId(), "결제 금액 불일치로 인한 자동 취소");
                Payment failed = payment.markFailed();
                paymentStatusUpdater.saveFailed(failed);
                throw new PaymentAmountMismatchException(
                        "결제 금액이 일치하지 않아 취소 처리되었습니다. 예상 결제 금액 =" + payment.getPrice() + ", 실제 결제 금액=" + detail.amount()
                );
            } catch (PortOneApiException e) {
                Payment cancelFailed = payment.markCancelFailed();
                paymentStatusUpdater.saveCancelFailed(cancelFailed);
                throw new PaymentCancelFailedException(
                        "취소 처리 실패  paymentId=" + command.paymentId()
                );
            }finally {
                paymentLockPort.unlock(command.userId(), payment.getPlan());
            }
        } else {
            Payment failed = payment.markFailed();
            paymentStatusUpdater.saveFailed(failed);
            paymentLockPort.unlock(command.userId(), payment.getPlan());
            throw new PaymentAmountMismatchException("결제가 완료되지 않았습니다.");
        }
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
