package com.wanted.momocity.review.infrastructure.adapter;

import com.wanted.momocity.lecture.application.port.LectureReviewQueryPort;
import com.wanted.momocity.review.infrastructure.persistence.ReviewJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class LectureReviewQueryAdapter implements LectureReviewQueryPort {

    private final ReviewJpaRepository reviewJpaRepository;

    @Override
    public ReviewStats getReviewStats(Long lectureId) {
        // 해당 강의의 평균 평점 조회
        double averageRating = reviewJpaRepository.findAverageRatingByLectureId(lectureId);

        // 해당 강의의 수강평 개수 조회
        int reviewCount = (int) reviewJpaRepository.countByLectureId(lectureId);

        // 수강평 통계 DTO
        return new ReviewStats(
                averageRating,
                reviewCount
        );
    }
}
