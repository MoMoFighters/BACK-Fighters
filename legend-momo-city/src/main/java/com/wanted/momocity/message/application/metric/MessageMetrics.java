package com.wanted.momocity.message.application.metric;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.DistributionSummary;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.springframework.stereotype.Component;

@Component
public class MessageMetrics {
    private final MeterRegistry meterRegistry;

    private final DistributionSummary roomMemberDistribution;
    private final Timer messageHistoryTimer;
    private final Timer chatRoomListTimer;
    private final Counter chatReenterCounter;
    private final Counter messageSendCounter;                 // 5. 메시지 발송 TPS 추적 카운터 추가

    public MessageMetrics(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;

        this.roomMemberDistribution = DistributionSummary.builder("momocity.chat.room.members")
                .description("생성·복구되는 방의 멤버 수 분포 파악")
                .register(meterRegistry);

        this.messageHistoryTimer = Timer.builder("momocity.message.history.latency")
                .description("메시지 내역 조회 지연 시간 성능 개선 지표")
                .register(meterRegistry);

        this.chatRoomListTimer = Timer.builder("momocity.chat.room.list.latency")
                .description("채팅방 목록 최적화 전/후 성능 측정 타이머")
                .register(meterRegistry);

        this.chatReenterCounter = Counter.builder("momocity.chat.reenter.count")
                .description("일대일 채팅방 퇴장 후 재입장 특수 트래픽 빈도 측정")
                .register(meterRegistry);

        this.messageSendCounter = Counter.builder("momocity.message.send.count")
                .description("전체 실시간 메시지 발송 총량 및 TPS 모니터링 지표")
                .register(meterRegistry);
    }

    public void recordRoomMemberCount(double memberCount) {
        roomMemberDistribution.record(memberCount);
    }

    public void incrementChatReenterCount() {
        chatReenterCounter.increment();
    }

    // 메시지 발송 카운터 체이닝 메서드
    public void incrementMessageSendCount() {
        messageSendCounter.increment();
    }

    public Timer getMessageHistoryTimer() { return this.messageHistoryTimer; }
    public Timer getChatRoomListTimer() { return this.chatRoomListTimer; }
}
