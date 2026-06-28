package com.wanted.momocity.friend.application.metric;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.stereotype.Component;

@Component
public class FriendMetrics {
    private final MeterRegistry meterRegistry;

    // 1. 전체 검색 스캔 횟수 카운터 (캐싱 필요성 검증용)
    private final Counter userSearchFullScanCounter;

    // 2. 권한/차단 예외 발생 횟수 카운터
    private final Counter friendPolicyViolationCounter;

    public FriendMetrics(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;

        this.userSearchFullScanCounter = Counter.builder("momocity.friend.search.fullscan")
                .description("사용자 검색 시 DB Full Scan 유발 횟수 - Redis 캐싱 전환 우선순위 지표")
                .register(meterRegistry);

        this.friendPolicyViolationCounter = Counter.builder("momocity.friend.error.denied")
                .description("친구/차단 권한 검증 실패 횟수 - 이상 접근 및 비정상 요청 감지")
                .register(meterRegistry);
    }

    public void recordSearchFullScan() {
        userSearchFullScanCounter.increment();
    }

    public void recordPolicyViolation() {
        friendPolicyViolationCounter.increment();
    }
}
