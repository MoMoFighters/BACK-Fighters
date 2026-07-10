package com.wanted.momocity.store.infrastructure.adapter;

import com.wanted.momocity.order.application.port.GetAllProductPort;
import com.wanted.momocity.order.domain.model.StoreItemResult;
import com.wanted.momocity.store.domain.model.Type;
import com.wanted.momocity.store.infrastructure.persistence.SpringDataStoreRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class GetAllProductAdapter implements GetAllProductPort {

    private final SpringDataStoreRepository springDataStoreRepository;

    @Override
    @Cacheable(value = "profileItems", key = "'ALL'")
    public List<StoreItemResult> getAllProfileItems() {
        return springDataStoreRepository.findAllByType(Type.PROFILE)
                .stream()
                .map(item -> new StoreItemResult(item.getId(), item.getName(), item.getUrl()))
                .toList();
    }
}
