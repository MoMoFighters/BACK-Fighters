package com.wanted.momocity.message.application.metric;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.DistributionSummary;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.springframework.stereotype.Component;

@Component
public class MessageMetrics {
    private final MeterRegistry meterRegistry;

    // 1. 읽음 처리 요청 횟수 (벌크 연산 튜닝 지표)
    private final Counter messageReadBulkCounter;

    // 2. 메시지 내역 조회 타이머 (N+1 및 인덱스 부하 측정용)
    private final Timer messageHistoryTimer;

    // 3. 채팅방 멤버 수 분포 레코드
    private final DistributionSummary roomMemberDistribution;

    public MessageMetrics(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;

        this.messageReadBulkCounter = Counter.builder("momocity.message.read.bulk")
                .description("메시지 읽음 처리 횟수 - @Modifying 벌크 연산 최적화 지표")
                .register(meterRegistry);

        this.messageHistoryTimer = Timer.builder("momocity.message.history.latency")
                .description("메시지 내역 조회 지연 시간 - N+1 및 인덱스 튜닝 성능 개선 지표")
                .register(meterRegistry);

        this.roomMemberDistribution = DistributionSummary.builder("momocity.chat.room.members")
                .description("채팅방 생성 시 멤버 수 분포 - 1:1 대화방 vs 다대다 활성 비율 파악")
                .register(meterRegistry);
    }

    public void recordReadBulk() {
        messageReadBulkCounter.increment();
    }

    public Timer getMessageHistoryTimer() {
        return this.messageHistoryTimer;
    }

    public void recordRoomMemberCount(double memberCount) {
        roomMemberDistribution.record(memberCount);
    }
}
