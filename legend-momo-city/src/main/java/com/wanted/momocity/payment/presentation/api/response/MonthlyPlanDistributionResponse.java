package com.wanted.momocity.payment.presentation.api.response;

import com.wanted.momocity.payment.domain.model.MonthlyPlanDistributionResult;

public record MonthlyPlanDistributionResponse(
        int month,
        long basic,
        long plus,
        long pro
) {
    public static MonthlyPlanDistributionResponse from(MonthlyPlanDistributionResult result) {
        return new MonthlyPlanDistributionResponse(result.month(), result.basic(), result.plus(), result.pro());
    }
}
