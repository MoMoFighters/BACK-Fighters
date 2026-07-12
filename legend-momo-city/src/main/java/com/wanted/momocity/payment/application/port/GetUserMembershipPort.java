package com.wanted.momocity.payment.application.port;

import com.wanted.momocity.payment.domain.model.Plan;

public interface GetUserMembershipPort {

    Plan getCurrentPlan(Long userId);
}
