package com.wanted.momocity.payment.application.command;

import com.wanted.momocity.payment.domain.model.Plan;

public record PaymentPrepareCommand(
        Long userId,
        Plan plan
) {
}
