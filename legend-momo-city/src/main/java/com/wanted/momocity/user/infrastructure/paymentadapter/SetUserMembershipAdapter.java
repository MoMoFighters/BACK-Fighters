package com.wanted.momocity.user.infrastructure.paymentadapter;

import com.wanted.momocity.payment.application.port.SetUserMembershipPort;
import com.wanted.momocity.payment.domain.model.Plan;
import com.wanted.momocity.user.domain.exception.UserNotFoundException;
import com.wanted.momocity.user.domain.model.Membership;
import com.wanted.momocity.user.infrastructure.persistence.SpringDataUserRepository;
import com.wanted.momocity.user.infrastructure.persistence.UserJpaEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
public class SetUserMembershipAdapter implements SetUserMembershipPort {

    // 사용자 구독 플랜 결제
    private final SpringDataUserRepository springDataUserRepository;

    @Override
    public void updateMembership(Long userId, Plan plan, LocalDateTime membershipStart) {
        UserJpaEntity user = springDataUserRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("사용자를 찾을 수 없습니다."));

        Membership membership = Membership.valueOf(plan.name());  // "PRO" → Membership.PRO
        springDataUserRepository.updateMembership(user.getId(), membership, membershipStart);
    }
}
