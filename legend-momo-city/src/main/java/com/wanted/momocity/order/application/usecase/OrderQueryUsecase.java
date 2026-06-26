package com.wanted.momocity.order.application.usecase;

import com.wanted.momocity.order.domain.model.OrderHistoryList;

public interface OrderQueryUsecase {

    // 포인트 내역 출력
    OrderHistoryList getOrderHistory(Long userId, int page, int size);
}
