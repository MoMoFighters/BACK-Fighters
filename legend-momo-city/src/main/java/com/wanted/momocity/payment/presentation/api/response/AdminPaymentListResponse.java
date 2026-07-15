package com.wanted.momocity.payment.presentation.api.response;

import com.wanted.momocity.payment.domain.model.AdminPaymentItem;
import com.wanted.momocity.payment.domain.model.AdminPaymentListResult;

import java.util.List;

public record AdminPaymentListResponse(
        List<AdminPaymentItem> payments,
        int page,
        int size,
        long totalElements,
        int totalPages
) {
    public static AdminPaymentListResponse from(AdminPaymentListResult result) {
        return new AdminPaymentListResponse(
                result.payments(),
                result.page(),
                result.size(),
                result.totalElements(),
                result.totalPages()
        );
    }
}
