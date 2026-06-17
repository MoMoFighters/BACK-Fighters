package com.wanted.momocity.enrollment.infrastructure.metrics;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.stereotype.Component;

@Component
public class BuildingMetrics {

    private final MeterRegistry meterRegistry;

    // ===== Counter =====
    private final Counter buildingAcquiredCounter;
    private final Counter buildingLevelUpCounter;

    public BuildingMetrics(MeterRegistry meterRegistry) {
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

    // 건물 획득 누적 횟수를 기록
    // 수강 신청 완료를 기반으로 발생
    // 서비스를 실질적으로 활용하는 활성 사용자의 서비스 이용 추이를 확인하는 지표
    public void recordBuildingAcquired() {
        buildingAcquiredCounter.increment();
    }

    // 건물 레벨업 누적 횟수를 기록
    // 스트릭 달성 기반으로 발생
    // 단기 참여가 아닌 장기 학습 지속성을 측정하는 지표
    public void recordBuildingLevelUp() {
        buildingLevelUpCounter.increment();
    }
}