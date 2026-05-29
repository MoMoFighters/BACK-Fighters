package com.wanted.momocity.lecture.domain.model;

import com.wanted.momocity.global.domain.common.exception.DomainRuleViolationException;
import com.wanted.momocity.lecture.presentation.api.response.AdminLectureListItemResponse;

import java.time.LocalDateTime;

// LectureAggregate는 강의 도메인 모델
public class LectureAggregate {

    private final Long id;
    private final Long teacherId;
    private final String title;
    private final String description;
    private final String thumbnailUrl;
    private final LectureCategory category;
    private final LectureStatus status;
    private final int completedUserCount;
    private final LocalDateTime createdAt;
    private final LocalDateTime updatedAt;

    private LectureAggregate(
            Long id,
            Long teacherId,
            String title,
            String description,
            String thumbnailUrl,
            LectureCategory category,
            LectureStatus status,
            int completedUserCount,
            LocalDateTime createdAt,
            LocalDateTime updatedAt
    ) {
        this.id = id;
        this.teacherId = teacherId;
        this.title = title;
        this.description = description;
        this.thumbnailUrl = thumbnailUrl;
        this.category = category;
        this.status = status;
        this.completedUserCount = completedUserCount;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    /* comment
     * 신규 강의를 생성할 때 사용하는 메서드
     * 수강 완료 인원 수는 처음 생성 시 0명으로 시작
     */
    public static LectureAggregate create(
            Long teacherId,
            String title,
            String description,
            String thumbnailUrl,
            LectureCategory category
    ) {
        validateTeacherId(teacherId);
        validateTitle(title);
        validateDescription(description);
        validateCategory(category);

        return new LectureAggregate(
                null,
                teacherId,
                title,
                description,
                thumbnailUrl,
                category,
                LectureStatus.WAITING,
                0,
                null,
                null
        );
    }

    /* comment
     * DB에서 조회한 값을 도메인 모델로 복원할 때 사용
     * JPA Entity에서 id, 생성일, 수정일을 포함해 Lecture로 변환할 때 호출
     */
    public static LectureAggregate restore(
            Long id,
            Long teacherId,
            String title,
            String description,
            String thumbnailUrl,
            LectureCategory category,
            LectureStatus status,
            int completedUserCount,
            LocalDateTime createdAt,
            LocalDateTime updatedAt
    ) {
        return new LectureAggregate(
                id,
                teacherId,
                title,
                description,
                thumbnailUrl,
                category,
                status,
                completedUserCount,
                createdAt,
                updatedAt
        );
    }

    /* comment
     * 강의 정보를 수정
     * 기존 강의의 id, teacherId, 상태, 생성일은 유지하고,
     * 제목/설명/썸네일/카테고리만 새 값으로 교체
     */
    public LectureAggregate update(
            String title,
            String description,
            String thumbnailUrl,
            LectureCategory category
    ) {
        validateTitle(title);
        validateDescription(description);
        validateCategory(category);

        return new LectureAggregate(
                id,
                teacherId,
                title,
                description,
                thumbnailUrl,
                category,
                status,
                completedUserCount,
                createdAt,
                updatedAt
        );
    }

    /* comment
     * 강의 상태를 변경
     * 강의의 기본 정보는 유지하고 status 값만 새로운 상태로 교체
     */
    public LectureAggregate changeStatus(LectureStatus newStatus) {
        validateStatus(newStatus);

        return new LectureAggregate(
                id,
                teacherId,
                title,
                description,
                thumbnailUrl,
                category,
                newStatus,
                completedUserCount,
                createdAt,
                updatedAt
        );
    }

    // 강의 목록 조회 응답 메서드
    public static AdminLectureListItemResponse from(
            LectureAggregate lecture,
            double averageRating,
            int reviewCount
    ) {
        return new AdminLectureListItemResponse(
                lecture.getId(),
                lecture.getTeacherId(),
                lecture.getTitle(),
                lecture.getDescription(),
                lecture.getThumbnailUrl(),
                lecture.getCategory().name(),
                lecture.getStatus().name(),
                lecture.getCompletedUserCount(),
                averageRating,
                reviewCount,
                lecture.getCreatedAt(),
                lecture.getUpdatedAt()
        );
    }

    /* comment
     * 요청한 teacherId가 이 강의의 소유자인지 확인
     * 수정/삭제 권한 검증에서 사용
     */
    public boolean isOwnedBy(Long teacherId) {
        return this.teacherId != null && this.teacherId.equals(teacherId);
    }

    // 강사 정보가 없으면 강의를 생성 X
    private static void validateTeacherId(Long teacherId) {
        if (teacherId == null) {
            throw new DomainRuleViolationException("강사 정보는 필수입니다.");
        }
    }

    // 강의 제목은 필수 입력값
    private static void validateTitle(String title) {
        if (title == null || title.isBlank()) {
            throw new DomainRuleViolationException("강의 제목은 필수입니다.");
        }
    }

    // 강의 설명은 필수 입력값
    private static void validateDescription(String description) {
        if (description == null || description.isBlank()) {
            throw new DomainRuleViolationException("강의 설명은 필수입니다.");
        }
    }

    // 강의 카테고리는 필수 입력값
    private static void validateCategory(LectureCategory category) {
        if (category == null) {
            throw new DomainRuleViolationException("강의 카테고리는 필수입니다.");
        }
    }

    // 강의 상태값은 필수
    private static void validateStatus(LectureStatus status) {
        if (status == null) {
            throw new DomainRuleViolationException("강의 상태는 필수입니다.");
        }
    }

    public Long getId() {
        return id;
    }

    public Long getTeacherId() {
        return teacherId;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public String getThumbnailUrl() {
        return thumbnailUrl;
    }

    public LectureCategory getCategory() {
        return category;
    }

    public LectureStatus getStatus() {
        return status;
    }

    public int getCompletedUserCount() {
        return completedUserCount;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
}