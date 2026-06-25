package com.wanted.momocity.user.infrastructure.point;

import com.wanted.momocity.global.application.point.PointChange;
import com.wanted.momocity.user.domain.exception.UserNotFoundException;
import com.wanted.momocity.user.infrastructure.persistence.SpringDataUserRepository;
import com.wanted.momocity.user.infrastructure.persistence.UserJpaEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ChangePointAdapter implements PointChange {

    private final SpringDataUserRepository springDataUserRepository;

    // 포인트 사용 = 포인트 -
    @Override
    public void usePoint(Long userId, Long amount) {
        UserJpaEntity user = springDataUserRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("해당 사용자를 찾을 수 없습니다."));
        user.decreasePoint(amount);
    }

    // 포인트 얻음 = 포인트 +
    @Override
    public void gainPoint(Long userId, Long amount) {
        UserJpaEntity user = springDataUserRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("해당 사용자를 찾을 수 없습니다."));
        user.increasePoint(amount);
    }
}
