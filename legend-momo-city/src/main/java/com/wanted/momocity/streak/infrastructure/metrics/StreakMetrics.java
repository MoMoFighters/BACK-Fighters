package com.wanted.momocity.streak.infrastructure.metrics;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.stereotype.Component;

/*
 * comment.
 *  Streak 도메인 메트릭 클래스
 *  - MeterRegistry 에 메트릭 등록 및 호출 메서드 제공
 *  - StreakCommandService 에서 주입받아 사용
 */
@Component
public class StreakMetrics {

    // Counter: Streak 신규 생성 횟수
    // 일별 활성 사용자 지표
    private final Counter streakCreatedCounter;

    // Counter: Streak 레벨업 횟수
    // 학습 지속성 측정
    private final Counter streakLevelUpCounter;

    public StreakMetrics(MeterRegistry meterRegistry) {

        this.streakCreatedCounter = Counter.builder("momocity.streak.created")
                .description("Streak 신규 생성 횟수 - 일별 활성 사용자 지표")
                .register(meterRegistry);

        this.streakLevelUpCounter = Counter.builder("momocity.streak.levelup")
                .description("Streak 레벨업 횟수 - 학습 지속성 측정")
                .register(meterRegistry);
    }

    public void recordStreakCreated() {
        streakCreatedCounter.increment();
    }

    public void recordStreakLevelUp() {
        streakLevelUpCounter.increment();
    }
}