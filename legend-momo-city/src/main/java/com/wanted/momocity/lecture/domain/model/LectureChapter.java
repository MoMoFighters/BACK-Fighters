package com.wanted.momocity.lecture.domain.model;

import com.wanted.momocity.global.domain.common.exception.DomainRuleViolationException;


import java.time.LocalDateTime;

// LectureChapter는 강의에 소속된 챕터 도메인
// 챕터의 제목, 순서, 동영상 상태 정보를 관리
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

    // 새 챕터를 등록할 때 사용
    // 동영상은 별도 API에서 등록하므로 여기서는 null로 시작
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

    // DB에서 조회한 챕터 정보를 도메인 모델로 복원할 때 사용
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

    /* comment
     * 챕터에 동영상을 등록
     * 기존 챕터 정보를 유지하면서 동영상 관련 값만 채운 새로운 LectureChapter 객체를 반환
     */
    public LectureChapter registerVideo(
            String videoUrl,
            Long videoSizeBytes,
            Integer durationSec,
            String originalFilename
    ) {
        validateVideoUrl(videoUrl);
        validateVideoSizeBytes(videoSizeBytes);
        validateDurationSec(durationSec);
        validateOriginalFilename(originalFilename);

        return new LectureChapter(
                id,
                lectureId,
                title,
                orderNo,
                videoUrl,
                videoSizeBytes,
                durationSec,
                VideoStatus.READY,
                originalFilename,
                createdAt,
                updatedAt
        );
    }

    // 챕터 동영상 처리 상태를 변경
    // 예: UPLOADING -> ENCODING -> READY -> FAILED
    public LectureChapter changeVideoStatus(VideoStatus videoStatus) {
        validateVideoStatus(videoStatus);

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

    /* comment
     * 이미 동영상이 등록된 챕터인지 확인
     * 서비스에서 중복 등록을 막을 때 사용
     */
    public boolean hasVideo() {
        return videoUrl != null && !videoUrl.isBlank();
    }

    /* comment
     * 이 챕터가 요청한 강의에 속한 챕터인지 확인
     * 다른 강의의 챕터에 영상을 등록하는 것을 막기 위해 사용
     */
    public boolean belongsTo(Long lectureId) {
        return this.lectureId.equals(lectureId);
    }

    // 챕터는 반드시 특정 강의에 소속
    private static void validateLectureId(Long lectureId) {
        if (lectureId == null) {
            throw new DomainRuleViolationException("강의 ID는 필수입니다.");
        }
    }

    // 챕터명은 필수
    private static void validateTitle(String title) {
        if (title == null || title.isBlank()) {
            throw new DomainRuleViolationException("챕터명은 필수입니다.");
        }
    }

    // 챕터 순서는 1 이상
    private static void validateOrderNo(int orderNo) {
        if (orderNo < 1) {
            throw new DomainRuleViolationException("챕터 순서는 1 이상이어야 합니다.");
        }
    }

    // 동영상 상태는 기본값을 포함해 항상 존재
    private static void validateVideoStatus(VideoStatus videoStatus) {
        if (videoStatus == null) {
            throw new DomainRuleViolationException("동영상 상태는 필수입니다.");
        }
    }

    // S3 업로드 후 반환된 동영상 URL은 필수
    private static void validateVideoUrl(String videoUrl) {
        if (videoUrl == null || videoUrl.isBlank()) {
            throw new DomainRuleViolationException("동영상 URL은 필수입니다.");
        }
    }

    /*
     * 동영상 파일 크기는 1byte 이상
     * 500MB 초과 검증은 S3 업로드 전 서비스에서 먼저 처리
     */
    private static void validateVideoSizeBytes(Long videoSizeBytes) {
        if (videoSizeBytes == null || videoSizeBytes < 1) {
            throw new DomainRuleViolationException("동영상 파일 크기는 1MB 이상이어야 합니다.");
        }
    }

    // 동영상 재생 시간은 1초 이상
    private static void validateDurationSec(Integer durationSec) {
        if (durationSec == null || durationSec < 1) {
            throw new DomainRuleViolationException("동영상 재생 시간은 1초 이상이어야 합니다.");
        }
    }

    // 원본 파일명은 응답과 관리 목적에 필요하므로 필수로 받음
    private static void validateOriginalFilename(String originalFilename) {
        if (originalFilename == null || originalFilename.isBlank()) {
            throw new DomainRuleViolationException("원본 파일명은 필수입니다.");
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