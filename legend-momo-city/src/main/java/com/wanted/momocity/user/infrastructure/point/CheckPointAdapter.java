package com.wanted.momocity.user.infrastructure.point;

import com.wanted.momocity.order.application.port.CheckPointPort;
import com.wanted.momocity.user.domain.exception.UserNotFoundException;
import com.wanted.momocity.user.infrastructure.persistence.SpringDataUserRepository;
import com.wanted.momocity.user.infrastructure.persistence.UserJpaEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CheckPointAdapter implements CheckPointPort {

    private final SpringDataUserRepository springDataUserRepository;

    // 구매할만큼 충분할 포인트를 사용자가 가지고 있는지 먼저 판단
    @Override
    public boolean isPointAble(Long userId, Long amount) {

        UserJpaEntity user = springDataUserRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("해당 사용자를 찾을 수 없습니다."));
        return user.getPoint() >= amount;
        // 사용자가 가진 현재 포인트가 필요한 가격보다 크거나 같으면 true 반환
    }
}
