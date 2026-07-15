package com.wanted.momocity.payment.presentation.api.response;

import com.wanted.momocity.payment.domain.model.PersonalPaymentListResult;
import com.wanted.momocity.payment.domain.model.Plan;
import com.wanted.momocity.payment.domain.model.Status;

import java.time.LocalDateTime;
import java.util.List;

public record PersonalPaymentListResponse(
        List<Item> payments,
        int page,
        int size,
        long totalElements,
        int totalPages
) {
    public record Item(
            long price,
            Plan plan,
            Status status,
            LocalDateTime createdAt
    ) {
    }

    public static PersonalPaymentListResponse from(PersonalPaymentListResult result) {
        List<Item> payments = result.payments().stream()
                .map(item -> new Item(item.price(), item.plan(), item.status(), item.createdAt()))
                .toList();

        return new PersonalPaymentListResponse(
                payments,
                result.page(),
                result.size(),
                result.totalElements(),
                result.totalPages()
        );
    }
}