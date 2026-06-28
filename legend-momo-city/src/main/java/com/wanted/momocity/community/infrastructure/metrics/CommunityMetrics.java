package com.wanted.momocity.community.infrastructure.metrics;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.springframework.stereotype.Component;

/*
 * comment.
 *  Community 도메인 메트릭 클래스
 *  - MeterRegistry 에 메트릭 등록 및 호출 메서드 제공
 *  - PostCommandService, PostQueryService 에서 주입받아 사용
 */
@Component
public class CommunityMetrics {

    private final MeterRegistry meterRegistry;

    // Counter: 게시글 작성 횟수
    // 커뮤니티 활성도 지표
    private final Counter postCreatedCounter;

    // Counter: 좋아요 누적 횟수
    // 콘텐츠 소비 패턴 파악
    private final Counter postLikedCounter;

    // Counter: 이미지 업로드 실패 횟수
    // S3/CloudFront 이상 감지
    private final Counter imageUploadFailedCounter;

    // Timer: 이미지 업로드 소요 시간
    // S3 업로드 성능 측정
    private final Timer imageUploadTimer;

    // Timer: 커서 페이지네이션 쿼리 소요 시간
    // 데이터 증가에 따른 쿼리 성능 추이 감시
    private final Timer postListQueryTimer;

    // Timer: 검색 쿼리 소요 시간
    // LIKE 쿼리 병목 조기 감지
    private final Timer postSearchQueryTimer;

    public CommunityMetrics(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;

        this.postCreatedCounter = Counter.builder("momocity.community.post.created")
                .description("게시글 작성 횟수 - 커뮤니티 활성도 지표")
                .register(meterRegistry);

        this.postLikedCounter = Counter.builder("momocity.community.post.liked")
                .description("좋아요 누적 횟수 - 콘텐츠 소비 패턴 파악")
                .register(meterRegistry);

        this.imageUploadFailedCounter = Counter.builder("momocity.community.image.upload.failed")
                .description("이미지 업로드 실패 횟수 - S3/CloudFront 이상 감지")
                .register(meterRegistry);

        this.imageUploadTimer = Timer.builder("momocity.community.image.upload.time")
                .description("이미지 업로드 소요 시간 - S3 업로드 성능 측정")
                .register(meterRegistry);

        this.postListQueryTimer = Timer.builder("momocity.community.post.list.query.time")
                .description("커서 페이지네이션 쿼리 소요 시간 - 데이터 증가 따른 성능 추이")
                .register(meterRegistry);

        this.postSearchQueryTimer = Timer.builder("momocity.community.post.search.query.time")
                .description("검색 쿼리 소요 시간 - LIKE 쿼리 병목 조기 감지")
                .register(meterRegistry);
    }

    public void recordPostCreated() {
        postCreatedCounter.increment();
    }

    public void recordPostLiked() {
        postLikedCounter.increment();
    }

    public void recordImageUploadFailed() {
        imageUploadFailedCounter.increment();
    }

    public Timer getImageUploadTimer() {
        return imageUploadTimer;
    }

    public Timer getPostListQueryTimer() {
        return postListQueryTimer;
    }

    public Timer getPostSearchQueryTimer() {
        return postSearchQueryTimer;
    }
}