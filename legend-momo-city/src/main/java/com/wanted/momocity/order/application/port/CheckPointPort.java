package com.wanted.momocity.order.application.port;

public interface CheckPointPort {

    // 구매를 할만큼의 포인트를 진짜 가졌는지
    boolean isPointAble(Long userId, Long amount);
}
