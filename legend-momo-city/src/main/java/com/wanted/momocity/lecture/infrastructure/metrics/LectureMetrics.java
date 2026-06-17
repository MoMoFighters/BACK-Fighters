package com.wanted.momocity.lecture.infrastructure.metrics;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.springframework.stereotype.Component;

@Component
public class LectureMetrics {

    private final MeterRegistry meterRegistry;

    private final Timer lectureUploadTimer;
    private final Timer lectureListTimer;

    public LectureMetrics(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;

        // Timer: 강의 등록 소요 시간
        // 트래픽 몰림, 영상 길이에 따른 S3 업로드 포함 전체 시간 측정
        this.lectureUploadTimer = Timer.builder("momocity.lecture.upload.duration")
                .description("강의 등록 소요 시간 - S3 업로드 포함")
                .register(meterRegistry);

        // Timer: 강의 목록 조회 소요 시간
        this.lectureListTimer = Timer.builder("momocity.lecture.list.duration")
                .description("강의 목록 조회 소요 시간")
                .register(meterRegistry);
    }

    public Timer.Sample startTimer() {
        return Timer.start(meterRegistry);
    }

    // 강의 등록 소요 시간 기록
    public void stopLectureUploadTimer(Timer.Sample sample) {
        sample.stop(lectureUploadTimer);
    }

    // 강의 목록 조회 소요 시간 기록
    public void stopLectureListTimer(Timer.Sample sample) {
        sample.stop(lectureListTimer);
    }
}