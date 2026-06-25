package com.wanted.momocity.store.infrastructure.adapter;

import com.wanted.momocity.order.application.port.LoadItemPort;
import com.wanted.momocity.order.domain.exception.ItemNotFoundException;
import com.wanted.momocity.order.domain.model.CheckItem;
import com.wanted.momocity.store.infrastructure.persistence.SpringDataStoreRepository;
import com.wanted.momocity.store.infrastructure.persistence.StoreJpaEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class LoadItemAdapter implements LoadItemPort {

    private final SpringDataStoreRepository springDataStoreRepository;

    // itemid 찾기
    @Override
    public CheckItem findByName(String itemName) {
        StoreJpaEntity entity = springDataStoreRepository.findByName(itemName)
                .orElseThrow(() -> new ItemNotFoundException("해당 상품은 존재하지 않습니다."));
        return new CheckItem(entity.getId(), entity.getPrice());
    }
}
