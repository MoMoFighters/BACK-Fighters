package com.wanted.momocity.payment.application.policy;

import com.wanted.momocity.payment.domain.exception.PaymentDowngradeNotAllowedException;
import com.wanted.momocity.payment.domain.exception.PaymentSamePlanException;
import com.wanted.momocity.payment.domain.model.Plan;
import org.springframework.stereotype.Component;

@Component
public class PaymentPolicy {

    /*comment
     * 플랜 변경 요청이 유효한지 검증하고, 결제해야 할 금액 계산
     * - 같은 플랜 요청 → 예외
     * - 다운그레이드 요청 → 예외 (결제 API 대상 아님, 구독 만료 시 자동 적용)
     * - 업그레이드 요청 → 차액 반환
     */

    public Long calculatePrice(Plan currentPlan, Plan targetPlan) {
        if (currentPlan == targetPlan) {
            throw new PaymentSamePlanException("이미 " + targetPlan + " 플랜을 이용 중입니다.");
        }

        if (targetPlan.isDowngradeFrom(currentPlan)) {
            throw new PaymentDowngradeNotAllowedException(
                    "다운그레이드는 결제가 필요하지 않습니다. 구독 만료 시점에 자동으로 적용됩니다."
            );
        }

        return targetPlan.getPrice() - currentPlan.getPrice();
    }
}
