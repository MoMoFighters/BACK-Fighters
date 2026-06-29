package com.wanted.momocity.order.infrastructure.adapter;

import com.wanted.momocity.order.infrastructure.persistence.SpringDataOrderRepository;
import com.wanted.momocity.store.application.port.CheckIsOrderedPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CheckIsOrderedAdapter implements CheckIsOrderedPort {

    private final SpringDataOrderRepository springDataOrderRepository;

    @Override
    public boolean checkIsOrdered(Long itemId, Long userId) {
        return springDataOrderRepository.existsByUserIdAndItemId(userId, itemId);
    }
}
