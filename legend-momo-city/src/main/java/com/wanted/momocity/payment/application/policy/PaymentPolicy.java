package com.wanted.momocity.payment.application.policy;

import com.wanted.momocity.payment.domain.exception.PaymentDowngradeNotAllowedException;
import com.wanted.momocity.payment.domain.exception.PaymentSamePlanException;
import com.wanted.momocity.payment.domain.model.Plan;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.LocalDateTime;

@Component
public class PaymentPolicy {

    private static final long MEMBERSHIP_PERIOD_DAYS = 30;
    private static final long RENEWAL_ALLOWED_DAYS = 7;
    private static final long MINIMUM_PAYMENT_AMOUNT = 100L;

    /*comment
     * 플랜 변경 요청이 유효한지 검증하고, 결제해야 할 금액 계산
     * - 같은 플랜 요청 → 만료 7일 이내면 갱신(정가), 아니면 예외
     * - 다운그레이드 요청 → 예외 (결제 API 대상 아님, 구독 취소 시 BASIC 전환)
     * - 업그레이드 요청 → 잔여일수 비례 차액 반환
     */

    public Long calculatePrice(Plan currentPlan, Plan targetPlan, LocalDateTime membershipStart) {
        LocalDateTime membershipUntil = membershipStart.plusDays(MEMBERSHIP_PERIOD_DAYS);
        boolean hasRemainingTime = membershipUntil.isAfter(LocalDateTime.now());

        if (currentPlan == targetPlan) {
            long remainingDays = calculateRemainingDaysCeil(membershipUntil); // 갱신 가능 여부 판단
            if (remainingDays <= RENEWAL_ALLOWED_DAYS) {
                return targetPlan.getPrice();
            }
            throw new PaymentSamePlanException("이미 " + targetPlan + " 플랜을 이용 중입니다.");
        }

        // BASIC에서 올라가거나, 이미 만료된 유료 플랜에서 전환하는 건 정가
        if (currentPlan == Plan.BASIC || !hasRemainingTime) {
            return targetPlan.getPrice();
        }

        if (targetPlan.isDowngradeFrom(currentPlan)) {
            throw new PaymentDowngradeNotAllowedException(
                    "플랜 다운그레이드는 지원하지 않습니다. 구독을 취소하시면 BASIC 플랜으로 전환됩니다."
            );
        }

        long remainingSeconds = Math.max(Duration.between(LocalDateTime.now(), membershipUntil).getSeconds(), 0);
        long totalSeconds = MEMBERSHIP_PERIOD_DAYS * 24 * 60 * 60;
        long priceDiff = targetPlan.getPrice() - currentPlan.getPrice();
        long amount = priceDiff * remainingSeconds / totalSeconds;

        return Math.max(amount, MINIMUM_PAYMENT_AMOUNT);
    }

    // 갱신 가능 여부 판단용
    private long calculateRemainingDaysCeil(LocalDateTime membershipUntil) {
        long remainingSeconds = Duration.between(LocalDateTime.now(), membershipUntil).getSeconds();
        if (remainingSeconds <= 0) return 0;
        return (long) Math.ceil(remainingSeconds / (double) (24 * 60 * 60));
    }
}
