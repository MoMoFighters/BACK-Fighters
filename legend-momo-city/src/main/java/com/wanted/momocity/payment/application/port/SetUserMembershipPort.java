package com.wanted.momocity.payment.application.port;

import com.wanted.momocity.payment.domain.model.Plan;

import java.time.LocalDateTime;

public interface SetUserMembershipPort {
    void updateMembership(Long userId, Plan plan, LocalDateTime membershipStart);

}
