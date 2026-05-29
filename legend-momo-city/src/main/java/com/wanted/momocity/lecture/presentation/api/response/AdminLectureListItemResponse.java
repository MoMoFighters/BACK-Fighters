package com.wanted.momocity.lecture.presentation.api.response;

import com.wanted.momocity.lecture.domain.model.LectureAggregate;

import java.time.LocalDateTime;

// 관리자 강의 목록에서 강의 1개를 표현하는 응답 DTO
public record AdminLectureListItemResponse(
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

    // 도메인 모델 LectureAggregate를 관리자 목록 응답 DTO로 변환
    public static AdminLectureListItemResponse from(LectureAggregate lecture) {
        return new AdminLectureListItemResponse(
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