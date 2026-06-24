package com.wanted.momocity.store.domain.repository;

import com.wanted.momocity.store.domain.model.Store;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface StoreRepository {

    // 전체 상품 목록 조회
    List<Store> getProductList(int page, int size);

    // 페이지네이션 페이지 수
    long countProductList();
}
