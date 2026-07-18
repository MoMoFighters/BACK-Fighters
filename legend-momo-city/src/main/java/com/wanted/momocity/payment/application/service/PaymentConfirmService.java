package com.wanted.momocity.payment.application.service;

import com.wanted.momocity.payment.application.port.GetUserMembershipPort;
import com.wanted.momocity.payment.application.port.PaymentLockPort;
import com.wanted.momocity.payment.application.port.PortOnePaymentPort;
import com.wanted.momocity.payment.application.port.SetUserMembershipPort;
import com.wanted.momocity.payment.application.supporter.PaymentStatusUpdater;
import com.wanted.momocity.payment.domain.exception.*;
import com.wanted.momocity.payment.domain.model.*;
import com.wanted.momocity.payment.domain.repository.PaymentRepository;
import com.wanted.momocity.payment.infrastructure.applier.PaymentRefetcher;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.time.LocalDateTime;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional
public class PaymentConfirmService {

    private final PaymentRepository paymentRepository;
    private final GetUserMembershipPort getUserMembershipPort;
    private final SetUserMembershipPort setUserMembershipPort;
    private final PortOnePaymentPort portOnePaymentPort;
    private final PaymentLockPort paymentLockPort;
    private final PaymentStatusUpdater paymentStatusUpdater;
    private final PaymentRefetcher paymentRefetcher;

    @Transactional
    public PaymentVerifyResult confirm(Payment payment) {
        // /verify와 웹훅이 같은 결제 건을 거의 동시에 확정 처리하려 드는 걸 막기 위한 락
        // 이 락을 못 잡으면 "누군가 이미 처리 중"이라는 뜻이므로 여기서 바로 튕겨낸다.
        if (!paymentLockPort.tryLock(payment.getUserId(), payment.getPlan())) {
            throw new PaymentAlreadyInProgressException(
                    "이미 처리 중인 결제 건입니다. paymentId=" + payment.getPaymentId());
        }

        /* comment
        *   이 트랜잭션이 커밋되든 롤백되든 끝난 직후 딱 한 번 락을 해제하도록
        *   이렇게 하면 DB 반영이 실제로 끝난 뒤에만 락이 풀리고
        *   아래에서 어떤 예외가 나든 unlock()이 누락되지 X
        */
        registerUnlockAfterCompletion(payment.getUserId(), payment.getPlan());

        // 락을 잡은 직후에 캐시 말고 진짜 최신 상태를 한 번 더 확인
        Payment freshPayment = paymentRefetcher.refetch(payment.getPaymentId());
        if (freshPayment.isFinalized()) {
            log.info("[confirm] 락 획득 전 이미 다른 요청이 처리를 완료함 - 중복 처리 방지 paymentId={}",
                    freshPayment.getPaymentId());
            return handleAlreadyFinalized(freshPayment);
        }

        PortOnePaymentDetail detail;
        try {
            detail = portOnePaymentPort.verifyPayment(payment.getPaymentId());
        } catch (PortOneApiException e) {
            log.error("[confirm] 포트원 API 호출 실패 paymentId={}", payment.getPaymentId(), e);
            throw e;
        }

        boolean amountMatches = detail.isPaid() && payment.getPrice().equals(detail.amount());

        if (amountMatches) {
            return handleSuccess(payment);}

        if (detail.isPaid()) {
            return handleAmountMismatch(payment, detail);}

        return handleNotPaid(payment);
    }

    // 재확인 결과 이미 처리가 끝나 있던 경우
    // SUCCESS면 그 결과를 그대로 돌려주rh / 그 외에는 /verify와 동일하게 예외 처리
    private PaymentVerifyResult handleAlreadyFinalized(Payment payment) {
        if (payment.getStatus() == Status.SUCCESS) {
            GetUserMembershipPort.UserMembership membership =
                    getUserMembershipPort.getUserMembership(payment.getUserId());
            LocalDateTime membershipUntil = membership.membershipStart().plusDays(30);
            return new PaymentVerifyResult(payment.getPaymentId(), payment.getStatus(), membershipUntil);
        }
        throw new PaymentAlreadyVerifiedException(
                "이미 처리된 결제 건입니다. status=" + payment.getStatus());
    }

    private void registerUnlockAfterCompletion(Long userId, Plan plan) {
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCompletion(int status) {
                paymentLockPort.unlock(userId, plan);
            }
        });
    }

    private PaymentVerifyResult handleSuccess(Payment payment) {
        Payment result = payment.markSuccess();

        GetUserMembershipPort.UserMembership membership =
                getUserMembershipPort.getUserMembership(payment.getUserId());
        LocalDateTime currentUntil = membership.membershipStart().plusDays(30);

        // 갱신 일정 분기
        boolean isSamePlanRenewal = membership.plan() == payment.getPlan();
        boolean hasRemainingTime = currentUntil.isAfter(LocalDateTime.now());

        LocalDateTime newMembershipStart = (isSamePlanRenewal && hasRemainingTime)
                ? currentUntil
                : LocalDateTime.now();

        // 사용자 멤버십 업데이트
        setUserMembershipPort.updateMembership(payment.getUserId(), payment.getPlan(), newMembershipStart);
        LocalDateTime newMembershipUntil = newMembershipStart.plusDays(30);

        paymentRepository.save(result);
        return new PaymentVerifyResult(result.getPaymentId(), result.getStatus(), newMembershipUntil);
    }

    private PaymentVerifyResult handleAmountMismatch(Payment payment, PortOnePaymentDetail detail) {
        try {
            portOnePaymentPort.cancelPayment(payment.getPaymentId(), "결제 금액 불일치로 인한 자동 취소");
            Payment failed = payment.markFailed();
            paymentStatusUpdater.saveFailed(failed);
            throw new PaymentAmountMismatchException(
                    "결제 금액이 일치하지 않아 취소 처리되었습니다. 예상 결제 금액=" + payment.getPrice()
                            + ", 실제 결제 금액=" + detail.amount()
            );
        } catch (PortOneApiException e) {
            Payment cancelFailed = payment.markCancelFailed();
            paymentStatusUpdater.saveCancelFailed(cancelFailed);
            throw new PaymentCancelFailedException("취소 처리 실패 paymentId=" + payment.getPaymentId());
        }
    }

    private PaymentVerifyResult handleNotPaid(Payment payment) {
        Payment failed = payment.markFailed();
        paymentStatusUpdater.saveFailed(failed);
        throw new PaymentAmountMismatchException("결제가 완료되지 않았습니다.");
    }
}
