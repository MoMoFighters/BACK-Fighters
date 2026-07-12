package com.wanted.momocity.payment.presentation.api.request;

import com.wanted.momocity.payment.domain.model.Plan;

public record PaymentPrepareRequest(
        Plan plan,
        Long price) {
}
