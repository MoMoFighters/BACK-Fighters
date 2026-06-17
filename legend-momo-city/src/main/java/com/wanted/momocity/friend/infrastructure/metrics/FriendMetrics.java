package com.wanted.momocity.friend.infrastructure.metrics;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.springframework.stereotype.Component;

@Component
public class FriendMetrics {

    private final MeterRegistry meterRegistry;

    private final Timer friendListTimer;

    public FriendMetrics(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;

        // Timer: 친구 목록 조회 소요 시간
        // 93ms, SQL 10개, N+1 발생 확인됨 — 최적화 전후 비교용
        this.friendListTimer = Timer.builder("momocity.friend.list.duration")
                .description("친구 목록 조회 소요 시간 - N+1 최적화 전후 비교")
                .register(meterRegistry);
    }

    public Timer.Sample startTimer() {
        return Timer.start(meterRegistry);
    }

    // 친구 목록 조회 소요 시간 기록
    public void stopFriendListTimer(Timer.Sample sample) {
        sample.stop(friendListTimer);
    }
}