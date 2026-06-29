package com.wanted.momocity.friend.application.metric;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.springframework.stereotype.Component;

@Component
public class FriendMetrics {
    private final MeterRegistry meterRegistry;

    // 💡 1. 사용자 검색 N+1 성능 지연 시간 측정용 타이머
    private final Timer userSearchTimer;

    // 2. 방명록 외부 인터페이스 예외 발생 횟수 카운터
    private final Counter guestbookPointErrorCounter;

    public FriendMetrics(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;

        this.userSearchTimer = Timer.builder("momocity.user.search.latency")
                .description("사용자 키워드 검색 및 강의명 N+1 조회 지연 시간 - Fetch Join 성능 지표")
                .register(meterRegistry);

        this.guestbookPointErrorCounter = Counter.builder("momocity.guestbook.point.error")
                .description("방명록 작성 후 포인트 팀 외부 인터페이스 연동 오류 횟수")
                .register(meterRegistry);
    }

    // 💡 타이머 객체를 서비스 단에 전달하기 위한 getter
    public Timer getUserSearchTimer() {
        return this.userSearchTimer;
    }

    // 💡 서비스단에서 받은 성공 여부 플래그로 에러 카운팅 판단
    public void recordGuestbookResult(boolean isSuccess) {
        if (!isSuccess) {
            guestbookPointErrorCounter.increment();
        }
    }
}
