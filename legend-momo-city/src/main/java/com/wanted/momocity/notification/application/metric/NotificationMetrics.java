package com.wanted.momocity.notification.application.metric;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.springframework.stereotype.Component;

@Component
public class NotificationMetrics {
    private final MeterRegistry meterRegistry;

    // 💡 알림 목록 조회 N+1 및 다대다 그룹화 가공 지연 시간 측정 타이머
    private final Timer notificationListTimer;

    public NotificationMetrics(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;

        this.notificationListTimer = Timer.builder("momocity.notification.list.latency")
                .description("알림 목록 조회 및 방별 그룹화 가공 지연 시간 - Fetch Join 성능 지표")
                .register(meterRegistry);
    }

    public Timer getNotificationListTimer() {
        return this.notificationListTimer;
    }
}
