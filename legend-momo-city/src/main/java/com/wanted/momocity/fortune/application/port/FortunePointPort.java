package com.wanted.momocity.fortune.application.port;

// 우세 서비스가 사용자 또는 포인트 기능에 요청할 작업
public interface FortunePointPort {

    // 사용자의 보유 포인트에서 지정한 금액을 차감
    boolean deductPointIfEnough(
            Long userId,
            // 뽑기할 때 사용 포인트
            Long amount
    );
}
