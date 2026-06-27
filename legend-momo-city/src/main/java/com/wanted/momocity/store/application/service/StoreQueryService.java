package com.wanted.momocity.store.application.service;

import com.wanted.momocity.store.application.port.GetUserPointPort;
import com.wanted.momocity.store.application.usecase.StoreQueryUsecase;
import com.wanted.momocity.store.domain.model.Store;
import com.wanted.momocity.store.domain.model.StoreListResult;
import com.wanted.momocity.store.domain.repository.StoreRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class StoreQueryService implements StoreQueryUsecase{

    private final StoreRepository storeRepository;
    private final GetUserPointPort getUserPointPort;

    // 전체 상품 목록 조회
    @Override
    public StoreListResult getProductList(Long userId, int page, int size) {

        Long point = getUserPointPort.getUserPoint(userId);

        List<Store> stores = storeRepository.getProductList(page, size);
        long totalElements = storeRepository.countProductList();
        int totalPages = (int) Math.ceil((double) totalElements / size);
        return new StoreListResult(stores, point, page, size, totalElements, totalPages);
    }
}
