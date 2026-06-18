package com.wanted.momocity.enrollment.infrastructure.metrics;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.stereotype.Component;

import java.util.concurrent.ConcurrentHashMap;

@Component
public class EnrollmentMetrics {

    private final ConcurrentHashMap<Long, Counter> enrollmentCounterCache = new ConcurrentHashMap<>();
    private final MeterRegistry meterRegistry;

    // Counter: 건물 획득 누적 횟수
    // 수강 신청 완료 기반 — 서비스 활성도 측정
    private final Counter buildingAcquiredCounter;

    // Counter: 건물 레벨업 누적 횟수
    // 강의 수강 완료 기반 — 장기 학습 지속성 측정
    private final Counter buildingLevelUpCounter;

    public EnrollmentMetrics(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;

        // Counter: 건물 획득 누적 횟수
        // 수강 신청 완료 기반 — 서비스 활성도 측정
        this.buildingAcquiredCounter = Counter.builder("momocity.building.acquired")
                .description("건물 획득 누적 횟수 - 수강 신청 완료 기반 활성 사용자 지표")
                .register(meterRegistry);

        // Counter: 건물 레벨업 누적 횟수
        // 강의 수강 완료 기반 — 장기 학습 지속성 측정
        this.buildingLevelUpCounter = Counter.builder("momocity.building.levelup")
                .description("건물 레벨업 누적 횟수 - 스트릭 달성 기반 학습 지속성 지표")
                .register(meterRegistry);
    }



    public void recordBuildingAcquired() {
        buildingAcquiredCounter.increment();
    }

    public void recordBuildingLevelUp() {
        buildingLevelUpCounter.increment();
    }

    // 개별 강의의 수강 신청 누적 횟수를 기록
    // 이 지표를 통해 인기 강의는 무엇인지 파악 - 그라파나에서 순위처럼 볼거임
    public void recordEnrollmentCreated(Long lectureId) {
        enrollmentCounterCache.computeIfAbsent(lectureId, id ->
                Counter.builder("momocity.enrollment.created")
                        .description("강의별 수강 신청 누적 횟수 - 인기 강의 파악")
                        .tag("lectureId", String.valueOf(id))
                        .register(meterRegistry)
        ).increment();
    }
}