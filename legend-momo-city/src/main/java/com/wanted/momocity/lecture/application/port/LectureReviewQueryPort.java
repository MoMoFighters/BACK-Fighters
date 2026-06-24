package com.wanted.momocity.lecture.application.port;

import java.time.LocalDate;
import java.time.LocalDateTime;

// Lecture 서비스가 review 정보를 조회하기 위한 인터페이스
public interface LectureReviewQueryPort {

    // 특정 강의의 평균 평점과 리뷰 개수 조회 메서드
    ReviewStats getReviewStats(Long lectureId);

    // 특정 강의의 리뷰 목록을 페이지 네이션으로 조회하는 메서드
    ReviewPage getReviews(Long lectureId, int page, int size);

    // 리뷰 통계 DTO
    record ReviewStats(
            // 평균 평점
            double averageRating,
            // 리뷰 개수
            int reviewCount
    ) {}

    // 리뷰 1개의 응답 정보 DTO
    record ReviewPage(
            Long reviewId,
            Long userId,
            String userName,
            String profileImageUrl,
            String content,
            int rating,
            LocalDateTime createdAt
    ){}

}
