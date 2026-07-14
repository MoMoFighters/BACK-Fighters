package com.wanted.momocity.user.infrastructure.paymentadapter;

import com.wanted.momocity.payment.application.port.GetUserMembershipPort;
import com.wanted.momocity.payment.domain.model.Plan;
import com.wanted.momocity.user.domain.exception.UserNotFoundException;
import com.wanted.momocity.user.domain.model.User;
import com.wanted.momocity.user.domain.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class GetUserMembershipAdapter implements GetUserMembershipPort {

    private final UserRepository userRepository;

    @Override
    public UserMembership getUserMembership(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("사용자를 찾을 수 없습니다."));

        Plan plan = switch (user.getMembership()) {
            case BASIC -> Plan.BASIC;
            case PLUS -> Plan.PLUS;
            case PRO -> Plan.PRO;
        };

        return new UserMembership(plan, user.getMembershipStart());
    }
}
