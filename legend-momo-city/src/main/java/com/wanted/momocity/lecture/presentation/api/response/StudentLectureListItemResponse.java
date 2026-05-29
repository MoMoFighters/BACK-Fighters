package com.wanted.momocity.lecture.presentation.api.response;

import java.time.LocalDateTime;

// 학생이 보는 강의 목록의 강의 1개 응답 DTO
public record StudentLectureListItemResponse(

        // 강의 고유 ID
        Long lectureId,

        // 강사를 식별하는 user ID
        Long teacherId,

        // 강사 이름
        String teacherName,

        // 강의 제목
        String title,

        // 강의 설명
        String description,

        // 강의 썸네일 이미지 URL
        String thumbnailUrl,

        // 강의 카테고리
        String category,

        // 강의 상태
        String lectureStatus,

        // 이 강의를 완료한 사용자 수
        int completedUserCount,

        // 리뷰 평균 평점
        double averageRating,

        // 리뷰 개수
        int reviewCount,

        // 로그인한 사용자가 이 강의를 수강신청했는지 여부 확인
        boolean isEnrolled,

        // 강의가 등록된 시간
        LocalDateTime createdAt
) {
}