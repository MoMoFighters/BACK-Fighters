package com.wanted.momocity.lecture.domain.model;

import com.wanted.momocity.global.domain.common.exception.DomainRuleViolationException;


import java.time.LocalDateTime;

// LectureChapter는 강의에 소속된 챕터 도메인 모델입니다.
// 챕터의 제목, 순서, 동영상 상태 정보를 관리합니다.
public class LectureChapter {

    private final Long id;
    private final Long lectureId;
    private final String title;
    private final int orderNo;
    private final String videoUrl;
    private final Long videoSizeBytes;
    private final Integer durationSec;
    private final VideoStatus videoStatus;
    private final String originalFilename;
    private final LocalDateTime createdAt;
    private final LocalDateTime updatedAt;

    private LectureChapter(
            Long id,
            Long lectureId,
            String title,
            int orderNo,
            String videoUrl,
            Long videoSizeBytes,
            Integer durationSec,
            VideoStatus videoStatus,
            String originalFilename,
            LocalDateTime createdAt,
            LocalDateTime updatedAt
    ) {
        validateLectureId(lectureId);
        validateTitle(title);
        validateOrderNo(orderNo);
        validateVideoStatus(videoStatus);

        this.id = id;
        this.lectureId = lectureId;
        this.title = title;
        this.orderNo = orderNo;
        this.videoUrl = videoUrl;
        this.videoSizeBytes = videoSizeBytes;
        this.durationSec = durationSec;
        this.videoStatus = videoStatus;
        this.originalFilename = originalFilename;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    // 새 챕터를 등록할 때 사용합니다.
    // 동영상은 별도 API에서 등록하므로 여기서는 null로 시작합니다.
    public static LectureChapter create(
            Long lectureId,
            String title,
            int orderNo
    ) {
        return new LectureChapter(
                null,
                lectureId,
                title,
                orderNo,
                null,
                null,
                null,
                VideoStatus.UPLOADING,
                null,
                null,
                null
        );
    }

    // DB에서 조회한 챕터 정보를 도메인 모델로 복원할 때 사용합니다.
    public static LectureChapter restore(
            Long id,
            Long lectureId,
            String title,
            int orderNo,
            String videoUrl,
            Long videoSizeBytes,
            Integer durationSec,
            VideoStatus videoStatus,
            String originalFilename,
            LocalDateTime createdAt,
            LocalDateTime updatedAt
    ) {
        return new LectureChapter(
                id,
                lectureId,
                title,
                orderNo,
                videoUrl,
                videoSizeBytes,
                durationSec,
                videoStatus,
                originalFilename,
                createdAt,
                updatedAt
        );
    }

    // 챕터는 반드시 특정 강의에 소속되어야 합니다.
    private static void validateLectureId(Long lectureId) {
        if (lectureId == null) {
            throw new DomainRuleViolationException("강의 ID는 필수입니다.");
        }
    }

    // 챕터명은 필수입니다.
    private static void validateTitle(String title) {
        if (title == null || title.isBlank()) {
            throw new DomainRuleViolationException("챕터명은 필수입니다.");
        }
    }

    // 챕터 순서는 1 이상이어야 합니다.
    private static void validateOrderNo(int orderNo) {
        if (orderNo < 1) {
            throw new DomainRuleViolationException("챕터 순서는 1 이상이어야 합니다.");
        }
    }

    // 동영상 상태는 기본값을 포함해 항상 존재해야 합니다.
    private static void validateVideoStatus(VideoStatus videoStatus) {
        if (videoStatus == null) {
            throw new DomainRuleViolationException("동영상 상태는 필수입니다.");
        }
    }

    public Long getId() {
        return id;
    }

    public Long getLectureId() {
        return lectureId;
    }

    public String getTitle() {
        return title;
    }

    public int getOrderNo() {
        return orderNo;
    }

    public String getVideoUrl() {
        return videoUrl;
    }

    public Long getVideoSizeBytes() {
        return videoSizeBytes;
    }

    public Integer getDurationSec() {
        return durationSec;
    }

    public VideoStatus getVideoStatus() {
        return videoStatus;
    }

    public String getOriginalFilename() {
        return originalFilename;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
}