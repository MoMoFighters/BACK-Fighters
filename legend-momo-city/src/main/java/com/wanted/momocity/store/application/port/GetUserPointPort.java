package com.wanted.momocity.store.application.port;

public interface GetUserPointPort {

    // 해당 사용자의 포인트 가져오기
    Long getUserPoint(Long userId);
}
