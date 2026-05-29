package com.wanted.momocity.lecture.presentation.api.response;

import com.wanted.momocity.lecture.domain.model.LectureAggregate;

import java.time.LocalDateTime;

// 관리자 강의 목록에서 강의 1개를 표현하는 응답 DTO
public record AdminLectureListItemResponse(
        Long lectureId,              // 강의 ID
        Long teacherId,              // 강사 ID
        String title,                // 강의 제목
        String description,          // 강의 설명
        String thumbnailUrl,         // 썸네일 URL
        String category,             // 강의 카테고리
        String lectureStatus,        // 강의 상태
        int completedUserCount,      // 수강 완료 인원
        double averageRating,        // 평균 평점
        int reviewCount,             // 리뷰 개수
        LocalDateTime createdAt,     // 강의 생성일
        LocalDateTime updatedAt      // 강의 수정일
) {

    /*
     * 도메인 모델 LectureAggregate를 관리자 목록 응답 DTO로 변환한다.
     * averageRating, reviewCount는 추후 review 패키지와 port 연결 후 실제 값으로 교체한다.
     */
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
}