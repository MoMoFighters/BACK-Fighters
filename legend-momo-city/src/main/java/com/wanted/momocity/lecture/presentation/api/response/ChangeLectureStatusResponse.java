package com.wanted.momocity.lecture.presentation.api.response;

import com.wanted.momocity.lecture.domain.model.LectureAggregate;

import java.time.LocalDateTime;

// 강의 상태 변경 성공 응답 DTO.
public record ChangeLectureStatusResponse(
        Long lectureId,
        String lectureStatus,
        LocalDateTime updatedAt
) {

    // LectureAggregate 도메인 모델을 응답 DTO로 변환
    public static ChangeLectureStatusResponse from(LectureAggregate lecture) {
        return new ChangeLectureStatusResponse(
                lecture.getId(),
                lecture.getStatus().name(),
                lecture.getUpdatedAt()
        );
    }
}