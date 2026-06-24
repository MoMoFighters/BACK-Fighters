package com.wanted.momocity.store.infrastructure.persistence;

import com.wanted.momocity.store.domain.model.Store;
import com.wanted.momocity.store.domain.repository.StoreRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository("StoreRepositoryAdapter")
@Transactional
@RequiredArgsConstructor
public class StoreRepositoryAdapter implements StoreRepository {

    private final SpringDataStoreRepository springDataStoreRepository;

    @Override
    public List<Store> getProductList(int page, int size) {
        return springDataStoreRepository.findAll(PageRequest.of(page - 1, size))
                .stream()
                .map(StoreJpaEntity::toDomain)
                .toList();
    }

    @Override
    public long countProductList() {
        return springDataStoreRepository.count();
    }
}
