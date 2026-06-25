package com.wanted.momocity.order.application.port;

public interface LoadItemPort {

    // 해당 상품이 실제로 있는지 판단
    boolean isRealItem(Long itemId);
}
