package com.wanted.momocity.global.infrastructure.metrics;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.springframework.stereotype.Component;

@Component
public class MomoMetrics {

    private final MeterRegistry meterRegistry;

    // ===== Timer =====
    private final Timer s3UploadTimer;
    private final Timer blacklistCheckTimer;

    // ===== Counter =====
    private final Counter s3UploadFailCounter;

    public MomoMetrics(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;

        // Timer: S3 업로드 소요 시간
        this.s3UploadTimer = Timer.builder("momocity.s3.upload.duration")
                .description("S3 파일 업로드 소요 시간")
                .register(meterRegistry);

        // Timer: 블랙리스트 Redis 조회 소요 시간
        // 모든 API 요청마다 실행 — S3 presigned URL 발급 시 135ms 소요 확인됨
        this.blacklistCheckTimer = Timer.builder("momocity.blacklist.check.duration")
                .description("요청마다 실행되는 블랙리스트 Redis 조회 소요 시간")
                .register(meterRegistry);

        // Counter: S3 업로드 실패 횟수
        this.s3UploadFailCounter = Counter.builder("momocity.s3.upload.failed")
                .description("S3 파일 업로드 실패 횟수")
                .register(meterRegistry);

    }

    // 작업 시작 시점의 시간을 기억
    // 실제 Timer는 작업이 끝난 뒤 sample.stop(timer)를 호출할 때 결정된다.
    // try-finally로 감싸서 성공/실패 여부와 관계없이 시간을 기록할 수 있다.
    public Timer.Sample startTimer() {
        return Timer.start(meterRegistry);
    }

    // S3 업로드 전체 소요 시간을 기록
    public void stopS3UploadTimer(Timer.Sample sample) {
        sample.stop(s3UploadTimer);
    }

    // S3 업로드 실패 횟수를 기록
    public void recordS3UploadFailed() {
        s3UploadFailCounter.increment();
    }

    // 블랙리스트 Redis 조회 소요 시간을 기록
    // 모든 API 요청마다 실행되므로 누적 지연이 전체 응답시간에 미치는 영향을 파악 가능
    public void stopBlacklistCheckTimer(Timer.Sample sample) {
        sample.stop(blacklistCheckTimer);
    }

}