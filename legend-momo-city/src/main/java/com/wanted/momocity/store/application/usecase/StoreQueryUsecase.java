package com.wanted.momocity.store.application.usecase;

import com.wanted.momocity.store.domain.model.StoreListResult;

public interface StoreQueryUsecase {

    // 상품 전체 목록 조회
    StoreListResult getProductList(Long userId, int page, int size);
    
}
