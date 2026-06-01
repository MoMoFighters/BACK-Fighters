package com.wanted.momocity.lecture.presentation.api.response;

import com.wanted.momocity.lecture.domain.model.LectureAggregate;

import java.time.LocalDateTime;

// CreateLectureResponse는 강의 등록 성공 시 프론트에 내려주는 응답 DTO
public record CreateLectureResponse(
        Long lectureId,
        Long teacherId,
        String title,
        String description,
        String thumbnailUrl,
        String category,
        String lectureStatus,
        int completedUserCount,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {

    // 도메인 모델 Lecture를 응답 DTO로 변환
    public static CreateLectureResponse from(LectureAggregate lecture) {
        return new CreateLectureResponse(
                lecture.getId(),
                lecture.getTeacherId(),
                lecture.getTitle(),
                lecture.getDescription(),
                lecture.getThumbnailUrl(),
                lecture.getCategory().name(),
                lecture.getStatus().name(),
                lecture.getCompletedUserCount(),
                lecture.getCreatedAt(),
                lecture.getUpdatedAt()
        );
    }
}