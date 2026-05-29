package com.wanted.momocity.lecture.presentation.api.response;

// LectureListItemResponse는 강의 목록의 강의 1개를 표현하는 응답 DTO
public record LectureListItemResponse(

        // 수강신청 ID
        Long enrollmentId,

        // 강의 ID
        Long lectureId,

        // 강의 제목
        String title,

        // 강의 썸네일 이미지 URL
        String thumbnailUrl,

        // 강의 카테고리
        String category,

        // 강의 상태
        String lectureStatus,

        // 로그인 사용자가 이 강의를 수강 신청했는지 여부
        boolean enrolled,

        // 전체 진도율
        int totalProgress,

        // 완료한 챕터 수
        int completedCount

) {
}