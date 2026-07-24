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
    private final String originalFilename;
    private final String chapterThumbnailUrl;
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
            String originalFilename,
            String chapterThumbnailUrl,
            LocalDateTime createdAt,
            LocalDateTime updatedAt
    ) {
        validateLectureId(lectureId);
        validateTitle(title);
        validateOrderNo(orderNo);

        this.id = id;
        this.lectureId = lectureId;
        this.title = title;
        this.orderNo = orderNo;
        this.videoUrl = videoUrl;
        this.videoSizeBytes = videoSizeBytes;
        this.durationSec = durationSec;
        this.originalFilename = originalFilename;
        this.chapterThumbnailUrl = chapterThumbnailUrl;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    // 챕터 썸네일 URL 검증
    private void validateChapterThumbnailUrl(String chapterThumbnailUrl) {
        if (chapterThumbnailUrl == null || chapterThumbnailUrl.isBlank()) {
            throw new DomainRuleViolationException("챕터 썸네일 URL은 필수입니다.");
        }
    }

    // 새 챕터를 등록할 때 사용
    // 동영상은 별도 API에서 등록하므로 여기서는 null로 시작
    public static LectureChapter create(
            Long lectureId,
            String title,
            int orderNo,
            String chapterThumbnailUrl
    ) {
        return new LectureChapter(
                null,
                lectureId,
                title,
                orderNo,
                null,
                null,
                null,
                null,
                chapterThumbnailUrl,
                null,
                null
        );
    }

    public static LectureChapter createWithoutThumbnail( // 썸네일 URL 없이 챕터 기본 정보만 생성하는 메서드
                                                         Long lectureId, // 강의 ID
                                                         String title, // 챕터 제목
                                                         int orderNo // 챕터 순서
    ) {
        return new LectureChapter( // LectureChapter 객체 생성
                null, // 새 챕터라 ID는 아직 없음
                lectureId, // 강의 ID 전달
                title, // 챕터 제목 전달
                orderNo, // 챕터 순서 전달
                null, // 동영상 URL은 아직 없음
                null, // 동영상 파일 크기는 아직 없음
                null, // 동영상 재생 시간은 아직 없음
                null, // 원본 파일명은 아직 없음
                null, // 썸네일 URL은 chapterId 생성 후 S3 업로드로 채움
                null, // 생성 시간은 JPA Auditing이 채움
                null // 수정 시간은 JPA Auditing이 채움
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
            String originalFilename,
            String chapterThumbnailUrl,
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
                originalFilename,
                chapterThumbnailUrl,
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
                originalFilename,
                chapterThumbnailUrl,
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

    // S3 업로드 후 반환된 동영상 URL은 필수
    private static void validateVideoUrl(String videoUrl) {
        if (videoUrl == null || videoUrl.isBlank()) {
            throw new DomainRuleViolationException("동영상 URL은 필수입니다.");
        }
    }

    /*
     * 동영상 파일 크기는 1byte 이상
     * 600MB 초과 검증은 S3 업로드 전 서비스에서 먼저 처리
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

    public String getChapterThumbnailUrl() {return  chapterThumbnailUrl;}

    public String getOriginalFilename() {
        return originalFilename;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public LectureChapter update(
            String title,
            int orderNo
    ) {
        // 챕터 제목이 비어있는지는 않은지 검증
         validateTitle(title);
         // 챕터 갯수가 1 이상인지 확인
         validateOrderNo(orderNo);

         return new LectureChapter(
                 id,
                 lectureId,
                 title,
                 orderNo,
                 videoUrl,
                 videoSizeBytes,
                 durationSec,
                 originalFilename,
                 chapterThumbnailUrl,
                 createdAt,
                 updatedAt
         );
    }

    public LectureChapter changedChapterThumbnailUrl(String chapterThumbnailUrl) {
        validateChapterThumbnailUrl(chapterThumbnailUrl);

        return new LectureChapter(
                id, // 기존 챕터 ID 유지
                lectureId, // 기존 강의 ID 유지
                title, // 기존 챕터 제목 유지
                orderNo, // 기존 챕터 순서 유지
                videoUrl, // 기존 동영상 URL 유지
                videoSizeBytes, // 기존 동영상 파일 크기 유지
                durationSec, // 기존 동영상 재생 시간 유지
                originalFilename, // 기존 원본 파일명 유지
                chapterThumbnailUrl, // 새 챕터 썸네일 URL 반영
                createdAt, // 기존 생성 시간 유지
                updatedAt // 기존 수정 시간 유지
        );
    }
}