package com.wanted.momocity.payment.presentation.api.response;

import com.wanted.momocity.payment.domain.model.MonthlySalesResult;

public record MonthlySalesResponse(
        int month,
        long sales
) {
    public static MonthlySalesResponse from(MonthlySalesResult result) {
        return new MonthlySalesResponse(result.month(), result.sales());
    }
}
