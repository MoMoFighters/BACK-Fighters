package com.wanted.momocity.order.domain.repositroy;

import com.wanted.momocity.order.domain.model.Reason;
import com.wanted.momocity.order.domain.model.Type;

public interface OrderRepository {

    // 상품 구매
    void makeOrder(Long userId, Reason reason, Type type, Long amount, Long itemId);

    // 이미 구매한 상품인지 확인
    boolean existsByUserIdAndItemId(Long userId, Long itemId);
}
