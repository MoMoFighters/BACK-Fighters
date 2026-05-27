package com.wanted.momocity.lecture.presentation.api.response;

import com.wanted.momocity.lecture.domain.model.Lecture;

import java.time.LocalDateTime;

/*
 * CreateLectureResponse는 강의 등록 성공 시 프론트에 내려주는 응답 DTO
 * API 명세의 data 필드 구조에 맞춰 생성된 강의의 기본 정보를 포함
 */
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

    /*
     * 도메인 모델 Lecture를 응답 DTO로 변환
     * Controller에서 응답 객체를 직접 조립하지 않도록 변환 책임을 이곳에 둔다.
     */
    public static CreateLectureResponse from(Lecture lecture) {
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