package com.wanted.momocity.lecture.presentation.api.response;


import com.wanted.momocity.lecture.domain.model.LectureAggregate;

import java.time.LocalDateTime;

// 강사 강의 목록에서 강의 1개를 표현하는 응답 DTO

/* comment
*   여기서 isEnabled는 넣지 않는다.
*   강사 목록 조회는 본인 강의를 출력을 하는데 수강여부가 아닌 관리 대상 여부가 중요하다.
* */
public record TeacherLectureListItemResponse(
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
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {

    // 도메인 모델 LectureAggregate를 강사용 목록 응답 DTO
    public static TeacherLectureListItemResponse from(
            LectureAggregate lecture,
            double averageRating,
            int reviewCount
    ) {
        return new TeacherLectureListItemResponse(
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
