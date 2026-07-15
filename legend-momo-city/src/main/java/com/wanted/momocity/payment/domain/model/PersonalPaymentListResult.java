package com.wanted.momocity.payment.domain.model;

import java.util.List;

public record PersonalPaymentListResult(
        List<PersonalPaymentItem> payments,
        int page,
        int size,
        long totalElements,
        int totalPages
) {
}
