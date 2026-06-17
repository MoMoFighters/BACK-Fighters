package com.wanted.momocity.message.infrastructure.metrics;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.springframework.stereotype.Component;

@Component
public class MessageMetrics {

    private final MeterRegistry meterRegistry;

    private final Timer messageHistoryTimer;
    private final Timer chatRoomListTimer;

    public MessageMetrics(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;

        // Timer: 메시지 내역 조회 소요 시간
        // 171ms, SQL 14개, N+1 발생 확인됨 — 최적화 before/after 비교용
        this.messageHistoryTimer = Timer.builder("momocity.message.history.duration")
                .description("메시지 내역 조회 소요 시간 - N+1 최적화 전후 비교")
                .register(meterRegistry);

        // Timer: 채팅방 목록 조회 소요 시간
        // 137ms, SQL 14개 확인됨
        this.chatRoomListTimer = Timer.builder("momocity.chatroom.list.duration")
                .description("채팅방 목록 조회 소요 시간")
                .register(meterRegistry);
    }

    public Timer.Sample startTimer() {
        return Timer.start(meterRegistry);
    }

    // 메시지 내역 조회 소요 시간 기록
    public void stopMessageHistoryTimer(Timer.Sample sample) {
        sample.stop(messageHistoryTimer);
    }

    // 채팅방 목록 조회 소요 시간 기록
    public void stopChatRoomListTimer(Timer.Sample sample) {
        sample.stop(chatRoomListTimer);
    }
}