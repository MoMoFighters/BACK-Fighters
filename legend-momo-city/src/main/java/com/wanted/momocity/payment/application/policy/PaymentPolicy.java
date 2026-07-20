package com.wanted.momocity.payment.application.policy;

import com.wanted.momocity.payment.domain.exception.PaymentDowngradeNotAllowedException;
import com.wanted.momocity.payment.domain.exception.PaymentSamePlanException;
import com.wanted.momocity.payment.domain.model.Plan;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

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
        boolean hasRemainingTime = membershipStart.plusDays(MEMBERSHIP_PERIOD_DAYS).isAfter(LocalDateTime.now());

        if (currentPlan == targetPlan) {
            long remainingDays = calculateRemainingDays(membershipStart);
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

        long remainingDays = calculateRemainingDays(membershipStart);
        long priceDiff = targetPlan.getPrice() - currentPlan.getPrice();
        long amount = priceDiff * remainingDays / MEMBERSHIP_PERIOD_DAYS;

        return Math.max(amount, MINIMUM_PAYMENT_AMOUNT);
    }


    private long calculateRemainingDays(LocalDateTime membershipStart) {
        LocalDateTime membershipUntil = membershipStart.plusDays(MEMBERSHIP_PERIOD_DAYS);
        long remaining = ChronoUnit.DAYS.between(LocalDateTime.now(), membershipUntil);
        return Math.max(remaining, 0);
        /*comment
        *  ChronoUnit.DAYS.between(start, end)는 두 시각 사이에 완전히 지나간 일수만 셈
        *  시/분/초는 버리고 정수로 내림 처리
        *  -> 15일 5시간 남아도 결과는 15 : 항상 내림 처리
        *  이미 만료돼서 음수가 나올 수 있어서 다음 줄에 Math.max(remaining, 0) */
    }
}
