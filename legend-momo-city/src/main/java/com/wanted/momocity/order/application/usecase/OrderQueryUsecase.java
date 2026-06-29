package com.wanted.momocity.order.application.usecase;

import com.wanted.momocity.order.domain.model.OrderHistoryList;
import com.wanted.momocity.order.domain.model.ProfileItemResult;

import java.util.List;

public interface OrderQueryUsecase {

    // 포인트 내역 출력
    OrderHistoryList getOrderHistory(Long userId, int page, int size);

    // 사용 가능한 프사 목록 출력
    List<ProfileItemResult> getAvailableProfile(Long userId);
}
