package com.wanted.momocity.store.application.usecase;

import com.wanted.momocity.store.domain.model.Store;
import com.wanted.momocity.store.domain.model.StoreListResult;

import java.util.List;

public interface StoreQueryUsecase {

    // 상품 전체 목록 조회
    StoreListResult getProductList(int page, int size);
    
}
