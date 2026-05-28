package com.wanted.momocity.lecture.presentation.api.response;

import java.time.LocalDateTime;

/**
 * 학생이 보는 강의 목록의 강의 1개 응답 DTO입니다.
 *
 * 기존 LectureListItemResponse는 수강 내역 쪽에서 사용할 수 있으므로,
 * 학생 강의 목록 전용 응답 DTO를 따로 분리합니다.
 */
public record StudentLectureListItemResponse(

        // 강의 고유 ID입니다.
        Long lectureId,

        // 강사를 식별하는 user ID입니다.
        Long teacherId,

        // 강사 이름입니다.
        String teacherName,

        // 강의 제목입니다.
        String title,

        // 강의 설명입니다.
        String description,

        // 강의 썸네일 이미지 URL입니다.
        String thumbnailUrl,

        // 강의 카테고리입니다.
        String category,

        // 강의 상태입니다. 학생 목록에서는 ACTIVE만 내려갑니다.
        String lectureStatus,

        // 이 강의를 완료한 사용자 수입니다.
        int completedUserCount,

        // 리뷰 평균 평점입니다.
        double averageRating,

        // 리뷰 개수입니다.
        int reviewCount,

        // 로그인한 사용자가 이 강의를 수강신청했는지 여부입니다.
        boolean isEnrolled,

        // 강의가 등록된 시간입니다.
        LocalDateTime createdAt
) {
}