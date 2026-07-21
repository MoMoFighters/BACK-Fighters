package com.wanted.momocity.friend.application.metric;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.springframework.stereotype.Component;

@Component
public class FriendMetrics {
    private final MeterRegistry meterRegistry;

    private final Timer userSearchTimer;
    private final Timer friendListTimer;
    private final Counter guestbookPointErrorCounter;

    public FriendMetrics(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;

        this.userSearchTimer = Timer.builder("momocity.user.search.latency")
                .description("사용자 키워드 검색 및 강의명 N+1 조회 지연 시간 - Fetch Join 성능 지표")
                .register(meterRegistry);

        this.friendListTimer = Timer.builder("momocity.friend.list.latency")
                .description("친구 목록 조회 및 강사 수강 강의 교집합 가공 지연 시간 측정")
                .register(meterRegistry);

        this.guestbookPointErrorCounter = Counter.builder("momocity.guestbook.point.error")
                .description("방명록 작성 후 포인트 팀 외부 인터페이스 연동 오류 횟수")
                .register(meterRegistry);
    }

    // 서비스단에서 meterRegistry 없이 바로 시간을 잴 수 있도록 스타트 대행
    public Timer.Sample start() {
        return Timer.start(this.meterRegistry);
    }

    public Timer getUserSearchTimer() { return this.userSearchTimer; }
    public Timer getFriendListTimer() { return this.friendListTimer; }

    public void recordGuestbookResult(boolean isSuccess) {
        if (!isSuccess) {
            guestbookPointErrorCounter.increment();
        }
    }
}
