package com.wanted.momocity.payment.domain.model;

import java.util.List;

public record AdminPaymentListResult(
        List<AdminPaymentItem> payments,
        int page,
        int size,
        long totalElements,
        int totalPages
) {
}
