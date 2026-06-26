package com.wanted.momocity.global.application.point;

public interface PointChange {
    // 포인트 사용
    void usePoint(Long userId, Long amount);

    // 포인트 얻음
    void gainPoint(Long userId, Long amount);

}
