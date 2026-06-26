package com.wanted.momocity.order.application.port;

import com.wanted.momocity.order.domain.model.StoreItemResult;

import java.util.List;

public interface GetAllProductPort {

    // 타입이 profile인 상품 목록 조회
     List<StoreItemResult> getAllProfileItems();
}
