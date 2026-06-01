package com.wanted.momocity.lecture.presentation.api.response;

import com.wanted.momocity.lecture.domain.model.LectureAggregate;

import java.time.LocalDateTime;

/* comment
 * 학생 강의 목록에서 강의 1개를 표현하는 응답 DTO
 * 최종 응답의 data.content 배열 안에 들어간다.
 */
public record StudentLectureListItemResponse(
        Long lectureId,              // 강의 ID
        Long teacherId,              // 강사 ID
        String teacherName,          // 강사 이름
        String title,                // 강의 제목
        String description,          // 강의 설명
        String thumbnailUrl,         // 썸네일 URL
        String category,             // 강의 카테고리
        String lectureStatus,        // 강의 상태
        int completedUserCount,      // 수강 완료 인원
        double averageRating,        // 평균 평점
        int reviewCount,             // 수강평 개수
        boolean isEnrolled,          // 현재 로그인한 학생의 수강 여부
        LocalDateTime createdAt      // 강의 생성일
) {
    /* comment
     * 도메인 모델을 화면 응답 DTO로 변환
     * 목록 조회에서는 이 객체 여러 개가 content 배열에 담긴다.
     */
    public static StudentLectureListItemResponse from(
            LectureAggregate lecture,
            String teacherName,
            double averageRating,
            int reviewCount,
            boolean isEnrolled
    ) {
        return new StudentLectureListItemResponse(
                lecture.getId(),
                lecture.getTeacherId(),
                teacherName,
                lecture.getTitle(),
                lecture.getDescription(),
                lecture.getThumbnailUrl(),
                lecture.getCategory().name(),
                lecture.getStatus().name(),
                lecture.getCompletedUserCount(),
                averageRating,
                reviewCount,
                isEnrolled,
                lecture.getCreatedAt()
        );
    }
}