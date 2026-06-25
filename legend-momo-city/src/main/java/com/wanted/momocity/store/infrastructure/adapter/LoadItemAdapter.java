package com.wanted.momocity.store.infrastructure.adapter;

import com.wanted.momocity.order.application.port.LoadItemPort;
import com.wanted.momocity.store.infrastructure.persistence.SpringDataStoreRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class LoadItemAdapter implements LoadItemPort {

    private final SpringDataStoreRepository springDataStoreRepository;

    // 사용자가 구매하고자 하는 상품이 실제로 있는지 판단
    @Override
    public boolean isRealItem(Long itemId) {
        return springDataStoreRepository.existsById(itemId);
        // 존재하면 true
    }
}
