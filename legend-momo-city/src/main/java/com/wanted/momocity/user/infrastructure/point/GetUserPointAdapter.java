package com.wanted.momocity.user.infrastructure.point;

import com.wanted.momocity.store.application.port.GetUserPointPort;
import com.wanted.momocity.user.infrastructure.persistence.SpringDataUserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class GetUserPointAdapter implements GetUserPointPort {

    private final SpringDataUserRepository springDataUserRepository;

    @Override
    public Long getUserPoint(Long userId) {
        return springDataUserRepository.findPointById(userId);
    }
}
