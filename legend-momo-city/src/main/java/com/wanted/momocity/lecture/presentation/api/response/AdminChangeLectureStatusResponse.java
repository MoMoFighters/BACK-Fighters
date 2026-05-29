package com.wanted.momocity.lecture.presentation.api.response;

import com.wanted.momocity.lecture.domain.model.LectureAggregate;

import java.time.LocalDateTime;

// 관리자 강의 상태 변경 성공 응답 DTO
public record AdminChangeLectureStatusResponse(
        Long lectureId,             // 상태가 변경된 강의 ID
        String lectureStatus,       // 변경된 강의 상태
        LocalDateTime updatedAt     // 상태 변경 시각
) {

    // 도메인 모델에서 필요한 값만 응답 DTO로 변환
    public static AdminChangeLectureStatusResponse from(LectureAggregate lecture) {
        return new AdminChangeLectureStatusResponse(
                lecture.getId(),
                lecture.getStatus().name(),
                lecture.getUpdatedAt()
        );
    }
}