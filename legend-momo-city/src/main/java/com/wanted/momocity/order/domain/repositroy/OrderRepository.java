package com.wanted.momocity.order.domain.repositroy;

import com.wanted.momocity.order.domain.model.ListResult;
import com.wanted.momocity.order.domain.model.Reason;
import com.wanted.momocity.order.domain.model.Type;

import java.util.List;

public interface OrderRepository {

    // 상품 구매
    void makeOrder(Long userId, Reason reason, Type type, Long amount, Long itemId);

    // 이미 구매한 상품인지 확인
    boolean existsByUserIdAndItemId(Long userId, Long itemId);

    // 포인트 내역 조회
    List<ListResult> getOrderHistory(Long userId, int page, int size);

    // 페이지네이션용
    long countByUserId(Long userId);

}
