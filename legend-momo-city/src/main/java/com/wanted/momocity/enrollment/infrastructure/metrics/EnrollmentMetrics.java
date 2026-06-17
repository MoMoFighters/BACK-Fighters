package com.wanted.momocity.enrollment.infrastructure.metrics;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.springframework.stereotype.Component;

@Component
public class EnrollmentMetrics {

    private final MeterRegistry meterRegistry;

    private final Timer enrollmentTimer;

    public EnrollmentMetrics(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;

        // Timer: 수강 신청 소요 시간
        this.enrollmentTimer = Timer.builder("momocity.enrollment.duration")
                .description("수강 신청 소요 시간")
                .register(meterRegistry);
    }

    // 작업 시작 시점의 시간을 기억
    // 실제 Timer는 작업이 끝난 뒤 sample.stop(timer)를 호출할 때 결정된다.
    // try-finally로 감싸서 성공/실패 여부와 관계없이 시간을 기록할 수 있다.
    public Timer.Sample startTimer() {
        return Timer.start(meterRegistry);
    }

    // 수강 신청 소요 시간 기록
    public void stopEnrollmentTimer(Timer.Sample sample) {
        sample.stop(enrollmentTimer);
    }

    // 개별 강의의 수강 신청 누적 횟수를 기록
    // 이 지표를 통해 인기 강의는 무엇인지 파악 - 그라파나에서 순위처럼 볼거임
    public void recordEnrollmentCreated(Long lectureId) {
        Counter.builder("momocity.enrollment.created")
                .description("강의별 수강 신청 누적 횟수 - 인기 강의 파악")
                .tag("lectureId", String.valueOf(lectureId))
                .register(meterRegistry)
                .increment();
    }
}