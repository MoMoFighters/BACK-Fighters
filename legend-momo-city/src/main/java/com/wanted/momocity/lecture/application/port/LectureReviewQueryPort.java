package com.wanted.momocity.lecture.application.port;

import java.util.List;
import java.util.Map;

// Lecture 서비스가 review 정보를 조회하기 위한 인터페이스
public interface LectureReviewQueryPort {

    // 특정 강의의 평균 평점과 리뷰 개수 조회 메서드
    ReviewStats getReviewStats(Long lectureId);

    // 여러 강의 리뷰 통계를 한 번에 처리 메서
    Map<Long, ReviewStats> getReviewStatsMap(List<Long> lectureIds);

    // 리뷰 통계 DTO
    record ReviewStats(
            // 평균 평점
            double averageRating,
            // 리뷰 개수
            int reviewCount
    ) {}
}
