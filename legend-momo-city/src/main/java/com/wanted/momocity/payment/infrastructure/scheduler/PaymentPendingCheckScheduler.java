package com.wanted.momocity.payment.infrastructure.scheduler;

import com.wanted.momocity.payment.application.port.GetUserMembershipPort;
import com.wanted.momocity.payment.application.port.PortOnePaymentPort;
import com.wanted.momocity.payment.domain.exception.PortOneApiException;
import com.wanted.momocity.payment.domain.model.Payment;
import com.wanted.momocity.payment.domain.model.Plan;
import com.wanted.momocity.payment.domain.model.PortOnePaymentDetail;
import com.wanted.momocity.payment.domain.model.Status;
import com.wanted.momocity.payment.infrastructure.applier.PaymentStatusApplier;
import com.wanted.momocity.payment.infrastructure.persistence.PaymentJpaEntity;
import com.wanted.momocity.payment.infrastructure.persistence.SpringDataPaymentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Component
@Slf4j
@RequiredArgsConstructor
public class PaymentPendingCheckScheduler {
    private final SpringDataPaymentRepository springDataPaymentRepository;
    private final PortOnePaymentPort portOnePaymentPort;
    private final GetUserMembershipPort getUserMembershipPort;
    private final PaymentStatusApplier paymentStatusApplier;

    /*comment
    *  포트원한테 결제 여부를 물어보는 api를 쐈는데 거기서 응답이 제대로 안 온 경우
    *  : 사용자의 결제가 실패했든 성공했든 포트원에서 결제 결과를 제대로 못 받아오니 처리 결과를 알 수 없음
    *  - 결제를 성공하면 success / 실패하면 failed인데 얘는 계속 pending상태로 남 게 됨
    *  - 그래서 스케줄러 돌려서 pending 상태인 애들에 대한 정보를 다시 가져오도록 해서
    *    확실해 success인지 failed인지 처리
    *   */

    @Scheduled(cron = "0 */10 * * * *") // 10분마다
    public void checkPendingPayments() {
        /*comment
        *  결제한 지 15분이 넘게 지난 결제 건 에 대해 처리하기 위함 */
        LocalDateTime threshold = LocalDateTime.now().minusMinutes(15);

        List<PaymentJpaEntity> pendingEntities = springDataPaymentRepository
                .findByStatusAndCreatedAtBefore(Status.PENDING, threshold);

        for (PaymentJpaEntity entity : pendingEntities) {
            checkAndUpdatePaymentStatus(entity.toDomain());
        }
    }

    private void checkAndUpdatePaymentStatus(Payment payment) {
        try {
            PortOnePaymentDetail detail = portOnePaymentPort.verifyPayment(payment.getPaymentId());
            boolean amountMatches = detail.isPaid() && payment.getPrice().equals(detail.amount());

            // 제대로 결제가 됐는데 조회만 안 된 상황
            if (amountMatches) {
                GetUserMembershipPort.UserMembership membership =
                        getUserMembershipPort.getUserMembership(payment.getUserId());

                LocalDateTime currentUntil = membership.membershipStart().plusDays(30);
                LocalDateTime newMembershipStart =
                        (membership.plan() != Plan.BASIC && currentUntil.isAfter(LocalDateTime.now()))
                                ? currentUntil
                                : LocalDateTime.now();

                paymentStatusApplier.applySuccess(payment, newMembershipStart);
                log.info("[PaymentPendingCheck] 누락된 결제 성공 건 복구 paymentId={}", payment.getPaymentId());
            }
            // 제대로 처리 안 된 경우
            else if (detail.isPaid()) {
                // 결제는 됐는데 금액이 다름 -> 취소 처리
                portOnePaymentPort.cancelPayment(payment.getPaymentId(), "결제 금액 불일치로 인한 자동 취소");
                paymentStatusApplier.applyFailed(payment);
                log.warn("[PaymentPendingCheck] 금액 불일치로 취소 처리 paymentId={}", payment.getPaymentId());
            }
            // 결제 안 됨
            else {
                paymentStatusApplier.applyFailed(payment);
                log.info("[PaymentPendingCheck] 미결제 확인, FAILED 처리 paymentId={}", payment.getPaymentId());
            }

        } catch (PortOneApiException e) {
            // 이번에도 조회 실패 -> 재시도
            log.error("[PaymentPendingCheck] 재조회 실패/ 재시도 paymentId={}", payment.getPaymentId(), e);
        } catch (Exception e) {
            // 저장 실패, unlock 실패, 예상 못한 예외 등 - 이 건만 스킵하고 나머지는 계속 처리
            log.error("[PaymentPendingCheck] 처리 중 예외 발생, 이 건은 스킵 paymentId={}", payment.getPaymentId(), e);
        }
    }
}
