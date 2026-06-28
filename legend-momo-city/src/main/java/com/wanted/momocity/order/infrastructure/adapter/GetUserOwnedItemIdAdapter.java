package com.wanted.momocity.order.infrastructure.adapter;

import com.wanted.momocity.order.infrastructure.persistence.SpringDataOrderRepository;
import com.wanted.momocity.store.application.port.GetUserOwnedItemIdPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class GetUserOwnedItemIdAdapter implements GetUserOwnedItemIdPort {

    private final SpringDataOrderRepository springDataOrderRepository;

    @Override
    public List<Long> userOwnedItemId(Long userId) {
        return springDataOrderRepository.getUserOwnedItemId(userId);
    }
}
