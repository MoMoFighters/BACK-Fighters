package com.wanted.momocity.store.application.port;

public interface CheckIsOrderedPort {

    // 사용자가 구매한건지 확인
    boolean checkIsOrdered(Long itemId, Long userId);
}
