package com.wanted.momocity.payment.application.port;

import com.wanted.momocity.payment.domain.model.Plan;

import java.time.LocalDateTime;

public interface GetUserMembershipPort {

    UserMembership getUserMembership(Long userId);

    record UserMembership(Plan plan, LocalDateTime membershipStart) {}
}
