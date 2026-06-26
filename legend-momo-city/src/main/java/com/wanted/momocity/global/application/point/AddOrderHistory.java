package com.wanted.momocity.global.application.point;

import com.wanted.momocity.order.domain.model.Reason;
import com.wanted.momocity.order.domain.model.Type;

public interface AddOrderHistory {
    // 새로운 프인트 내역 추가
    void saveOrderHistory(Long userId, Reason reason, Type type, Long amount);
}
