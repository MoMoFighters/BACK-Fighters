package com.wanted.momocity.lecture.presentation.api.response;

import com.wanted.momocity.lecture.domain.model.LectureAggregate;
import com.wanted.momocity.lecture.domain.model.LectureChapter;

import java.time.LocalDateTime;
import java.util.List;

// 관리자 강의 상세 조회 응답 DTO
public record AdminLectureDetailResponse(
        Long lectureId,
        Long teacherId,
        String title,
        String description,
        String thumbnailUrl,
        String category,
        String lectureStatus,
        int completedUserCount,
        double averageRating,
        int reviewCount,
        List<AdminLectureChapterResponse> chapters,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {

    // 강의 도메인 모델과 챕터 목록을 관리자 상세 응답 DTO로 변환한다.
    public static AdminLectureDetailResponse from(
            LectureAggregate lecture,
            List<LectureChapter> chapters,
            double averageRating,
            int reviewCount
    ) {
        return new AdminLectureDetailResponse(
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
                chapters.stream()
                        .map(AdminLectureChapterResponse::from)
                        .toList(),
                lecture.getCreatedAt(),
                lecture.getUpdatedAt()
        );
    }
}