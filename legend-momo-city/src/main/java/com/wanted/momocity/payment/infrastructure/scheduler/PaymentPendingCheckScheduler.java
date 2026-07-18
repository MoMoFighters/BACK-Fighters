package com.wanted.momocity.payment.infrastructure.scheduler;

import com.wanted.momocity.payment.application.service.PaymentConfirmService;
import com.wanted.momocity.payment.domain.exception.PaymentAlreadyInProgressException;
import com.wanted.momocity.payment.domain.exception.PortOneApiException;
import com.wanted.momocity.payment.domain.model.Payment;
import com.wanted.momocity.payment.domain.model.Status;
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
    private final PaymentConfirmService paymentConfirmService;


    /*comment
    *  포트원한테 결제 여부를 물어보는 api를 쐈는데 거기서 응답이 제대로 안 온 경우
    *  : 사용자의 결제가 실패했든 성공했든 포트원에서 결제 결과를 제대로 못 받아오니 처리 결과를 알 수 없음
    *  - 결제를 성공하면 success / 실패하면 failed인데 얘는 계속 pending상태로 남 게 됨
    *  - 그래서 스케줄러 돌려서 pending 상태인 애들에 대한 정보를 다시 가져오도록 해서
    *    확실해 success인지 failed인지 처리
    *   */


    @Scheduled(cron = "0 */10 * * * *")
    public void checkPendingPayments() {
        LocalDateTime threshold = LocalDateTime.now().minusMinutes(15);
        List<PaymentJpaEntity> pendingEntities = springDataPaymentRepository
                .findByStatusAndCreatedAtBefore(Status.PENDING, threshold);

        for (PaymentJpaEntity entity : pendingEntities) {
            checkAndUpdatePaymentStatus(entity.toDomain());
        }
    }

    private void checkAndUpdatePaymentStatus(Payment payment) {
        try {
            paymentConfirmService.confirm(payment);
            log.info("[PaymentPendingCheck] 확정 처리 완료 paymentId={}", payment.getPaymentId());
        } catch (PaymentAlreadyInProgressException e) {
            log.info("[PaymentPendingCheck] 다른 요청이 처리 중 - 스킵 paymentId={}", payment.getPaymentId());
        } catch (PortOneApiException e) {
            log.error("[PaymentPendingCheck] 재조회 실패 - 다음 주기에 재시도 paymentId={}", payment.getPaymentId(), e);
        } catch (Exception e) {
            log.error("[PaymentPendingCheck] 처리 중 예외 발생, 이 건은 스킵 paymentId={}", payment.getPaymentId(), e);
        }
    }
}
