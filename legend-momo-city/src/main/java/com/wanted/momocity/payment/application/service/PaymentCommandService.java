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
import com.wanted.momocity.payment.infrastructure.applier.PaymentRefetcher;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
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
    private final PaymentRefetcher paymentRefetcher;

    // 결제 준비 - 결제 금액 저장용
    @Override
    public PaymentPrepareResult paymentPrepare(PaymentPrepareCommand command) {

        // 플랜 변경 유효성 검증 + 결제 금액 계산
        GetUserMembershipPort.UserMembership membership = getUserMembershipPort.getUserMembership(command.userId());
        Long price = paymentPolicy.calculatePrice(membership.plan(), command.plan(),membership.membershipStart());

        // 중복 결제 방지
        if (!paymentLockPort.tryLock(command.userId(), command.plan())) {
            throw new PaymentAlreadyInProgressException("이미 진행 중인 결제가 있습니다.");
        }

        try {
            String paymentId = UUID.randomUUID().toString();
            Payment payment = Payment.createPending(command.userId(), paymentId, null,command.plan(), price);
            Payment prepare = paymentRepository.save(payment);
            paymentLockPort.unlock(command.userId(), command.plan());
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
            if (payment.getStatus() == Status.SUCCESS) {
                return toVerifyResult(payment);
            }
            throw new PaymentAlreadyVerifiedException(
                    "이미 처리된 결제 건입니다. status=" + payment.getStatus());
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
                Thread.sleep(200);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }

            // REQUIRES_NEW로 새 트랜잭션을 열어야 웹훅이 커밋해놓은 최신 상태를 볼 수 있음 !!!
            Payment refreshed = paymentRefetcher.refetch(paymentId);

            if (refreshed.isFinalized()) {
                if (refreshed.getStatus() == Status.SUCCESS) {
                    return toVerifyResult(refreshed);
                }
                throw new PaymentAlreadyVerifiedException(
                        "이미 처리된 결제 건입니다. status=" + refreshed.getStatus());
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

        List<Payment> refundTargets = new ArrayList<>();
        refundTargets.add(payment);

        // PRO 결제를 취소하는 경우에만
        boolean plusPaymentExists = false;

        if (payment.getPlan() == Plan.PRO) {
            GetUserMembershipPort.UserMembership membership =
                    getUserMembershipPort.getUserMembership(command.userId());

            var existingPlus = paymentRepository
                    .findUnrefundedSuccessPayment(command.userId(), Plan.PLUS, membership.membershipStart());

            plusPaymentExists = existingPlus.isPresent();

            existingPlus
                    .filter(cancelPolicy::isWithinRefundPeriod)
                    .ifPresent(refundTargets::add);
        }

        // PLUS 결제를 취소하려는데 그 위에 아직 살아있는 PRO 결제가 있으면 막음
        if (payment.getPlan() == Plan.PLUS) {
            GetUserMembershipPort.UserMembership membership =
                    getUserMembershipPort.getUserMembership(command.userId());

            boolean proExists = paymentRepository
                    .findUnrefundedSuccessPayment(command.userId(), Plan.PRO, membership.membershipStart())
                    .isPresent();

            if (proExists) {
                throw new PaymentRefundNotAllowedException(
                        "상위 플랜(PRO) 결제가 남아있어 이 결제는 단독으로 환불할 수 없습니다.");
            }
        }

        // 동시 취소 요청 방지
        List<Plan> lockPlans = refundTargets.stream().map(Payment::getPlan).distinct().toList();
        List<Plan> lockedPlans = new ArrayList<>();

        for (Plan plan : lockPlans) {
            if (!paymentLockPort.tryLock(command.userId(), plan)) {
                lockedPlans.forEach(p -> paymentLockPort.unlock(command.userId(), p));
                throw new PaymentAlreadyInProgressException("이미 처리 중인 요청이 있습니다.");
            }
            lockedPlans.add(plan);
        }

        List<Payment> succeeded = new ArrayList<>();
        try {
            for (Payment target : refundTargets) {
                try {
                    portOnePaymentPort.cancelPayment(target.getPaymentId(), "사용자 요청에 의한 환불");
                    Payment refund = Payment.createRefund(target, UUID.randomUUID().toString());
                    paymentStatusUpdater.saveRefund(refund); // 즉시 독립 커밋
                    succeeded.add(target);
                } catch (Exception e) {
                    log.error("[cancel] 환불 처리 실패 paymentId={}, userId={}",
                            target.getPaymentId(), command.userId(), e);
                    Payment cancelFailed = Payment.createCancelFailed(target, UUID.randomUUID().toString());
                    paymentStatusUpdater.saveCancelFailed(cancelFailed); // 실패한 target 기준

                    applyMembership(command.userId(), payment, succeeded, plusPaymentExists);

                    throw new PaymentCancelFailedException(
                            "일부 결제 건 환불 실패. 성공: "
                                    + succeeded.stream().map(Payment::getPaymentId).toList()
                                    + ", 실패: " + target.getPaymentId() + " (관리자 확인 필요)");
                }
            }

            applyMembership(command.userId(), payment, succeeded, plusPaymentExists);

        } finally {
            lockedPlans.forEach(p -> paymentLockPort.unlock(command.userId(), p));
        }

    }

    private void applyMembership(Long userId, Payment mainTarget, List<Payment> succeeded, boolean plusPaymentExists) {
        cancelPolicy.resolveResultPlan(mainTarget, succeeded, plusPaymentExists).ifPresent(resultPlan -> {
            LocalDateTime resultMembershipStart = resultPlan == Plan.BASIC
                    ? LocalDateTime.now()
                    : getUserMembershipPort.getUserMembership(userId).membershipStart();

            paymentStatusUpdater.updateMembershipIndependently(userId, resultPlan, resultMembershipStart);
        });
    }
}
