package com.wanted.momocity.lecture.application.port;

// Lecture 서비스가 review 정보를 조회하기 위한 인터페이스
public interface LectureReviewQueryPort {

    // 특정 강의의 평균 평점과 리뷰 개수 조회 메서드
    ReviewStats getReviewStats(Long lectureId);

    // 리뷰 통계 DTO
    record ReviewStats(
            // 평균 평점
            double averageRating,
            // 리뷰 개수
            int reviewCount
    ) {}
}
