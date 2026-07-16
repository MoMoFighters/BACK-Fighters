package com.wanted.momocity.payment.domain.model;

public record MonthlyPlanDistributionResult(
        int month,
        long basic,
        long plus,
        long pro
) {
}
