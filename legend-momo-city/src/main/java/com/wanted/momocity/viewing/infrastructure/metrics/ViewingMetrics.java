package com.wanted.momocity.viewing.infrastructure.metrics;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.DistributionSummary;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.springframework.stereotype.Component;

/*
 * comment.
 *  Viewing 도메인 메트릭 클래스
 *  - MeterRegistry 에 메트릭 등록 및 호출 메서드 제공
 *  - ViewingCommandService, ViewingQueryService 에서 주입받아 사용
 */
@Component
public class ViewingMetrics {

    private final MeterRegistry meterRegistry;

    // Counter: 낙관적 락 충돌 횟수
    // 갑자기 튀면 특정 챕터에 트래픽 몰린 신호
    private final Counter optimisticLockConflictCounter;

    // Counter: 챕터 완료 횟수
    // 학습 지속성 핵심 지표, 수강신청 횟수 대비 실제 학습 전환율 파악
    private final Counter chapterCompletedCounter;

    // Counter: 캐시 히트 횟수 (chapter + lecture 통합)
    // Redis 도입 효과 수치화
    private final Counter cacheHitCounter;

    // Counter: 캐시 미스 횟수 (chapter + lecture 통합)
    // 캐시 히트율 = hit / (hit + miss) 로 계산
    private final Counter cacheMissCounter;

    // Counter: 건너뛰기 차단 횟수
    // hasMeaningfulProgress = false 시 증가 → 어뷰징 패턴 탐지
    private final Counter skipBlockedCounter;

    // Timer: 진척도 저장 처리 시간
    // 가장 빈번한 로직, 지연 누적되면 UX 직결
    private final Timer saveProgressTimer;

    // Timer: S3 Presigned URL 발급 시간
    // 외부 호출이라 AWS 장애 조기 감지
    private final Timer s3PresignedUrlTimer;

    // DistributionSummary: watchedSeconds 분포
    // 사용자가 몇 초씩 보내는지 분포로 몰아보기 vs 짧게 보기 패턴 파악
    private final DistributionSummary watchedSecondsDistribution;

    public ViewingMetrics(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;

        // Counter: 낙관적 락 충돌 횟수
        this.optimisticLockConflictCounter = Counter.builder("momocity.viewing.optimistic.lock.conflict")
                .description("낙관적 락 충돌 횟수 - 트래픽 집중 신호 감지")
                .register(meterRegistry);

        // Counter: 챕터 완료 횟수
        this.chapterCompletedCounter = Counter.builder("momocity.viewing.chapter.completed")
                .description("챕터 완료 횟수 - 학습 전환율 지표")
                .register(meterRegistry);

        // Counter: 캐시 히트 횟수
        this.cacheHitCounter = Counter.builder("momocity.viewing.cache.hit")
                .description("캐시 히트 횟수 - Redis 캐싱 효과 측정")
                .register(meterRegistry);

        // Counter: 캐시 미스 횟수
        this.cacheMissCounter = Counter.builder("momocity.viewing.cache.miss")
                .description("캐시 미스 횟수 - DB 조회 빈도 측정")
                .register(meterRegistry);

        // Counter: 건너뛰기 차단 횟수
        this.skipBlockedCounter = Counter.builder("momocity.viewing.skip.blocked")
                .description("10초 초과 건너뛰기 차단 횟수 - 어뷰징 패턴 감지")
                .register(meterRegistry);

        // Timer: 진척도 저장 처리 시간
        this.saveProgressTimer = Timer.builder("momocity.viewing.save.progress.time")
                .description("진척도 저장 처리 시간 - 가장 빈번한 로직 성능 측정")
                .register(meterRegistry);

        // Timer: S3 Presigned URL 발급 시간
        this.s3PresignedUrlTimer = Timer.builder("momocity.viewing.s3.presigned.url.time")
                .description("S3 Presigned URL 발급 시간 - 외부 호출 성능 및 AWS 장애 감지")
                .register(meterRegistry);

        // DistributionSummary: watchedSeconds 분포
        this.watchedSecondsDistribution = DistributionSummary.builder("momocity.viewing.watched.seconds")
                .description("watchedSeconds 분포 - 사용자 시청 패턴 파악")
                .baseUnit("seconds")
                .register(meterRegistry);
    }

    // 낙관적 락 충돌 횟수 증가
    public void recordOptimisticLockConflict() {
        optimisticLockConflictCounter.increment();
    }

    // 챕터 완료 횟수 증가
    public void recordChapterCompleted() {
        chapterCompletedCounter.increment();
    }

    // 캐시 히트 횟수 증가
    public void recordCacheHit() {
        cacheHitCounter.increment();
    }

    // 캐시 미스 횟수 증가
    public void recordCacheMiss() {
        cacheMissCounter.increment();
    }

    // 건너뛰기 차단 횟수 증가
    public void recordSkipBlocked() {
        skipBlockedCounter.increment();
    }

    // 진척도 저장 Timer 반환 (Timer.record() 로 감싸서 사용)
    public Timer getSaveProgressTimer() {
        return saveProgressTimer;
    }

    // S3 Presigned URL Timer 반환
    public Timer getS3PresignedUrlTimer() {
        return s3PresignedUrlTimer;
    }

    // watchedSeconds 분포 기록
    public void recordWatchedSeconds(int watchedSeconds) {
        watchedSecondsDistribution.record(watchedSeconds);
    }
}