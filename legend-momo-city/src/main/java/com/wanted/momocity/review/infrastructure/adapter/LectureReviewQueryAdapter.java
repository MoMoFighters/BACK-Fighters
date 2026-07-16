package com.wanted.momocity.review.infrastructure.adapter;

import com.wanted.momocity.lecture.application.port.LectureReviewQueryPort;
import com.wanted.momocity.review.domain.model.ReviewStatus;
import com.wanted.momocity.review.infrastructure.persistence.ReviewJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class LectureReviewQueryAdapter implements LectureReviewQueryPort {

    private final ReviewJpaRepository reviewJpaRepository;

    @Override
    public ReviewStats getReviewStats(Long lectureId) {
        // 해당 강의의 평균 평점 조회
        double averageRating = reviewJpaRepository.findAverageRatingByLectureId(lectureId);

        // 해당 강의의 수강평 개수 조회
        int reviewCount = (int) reviewJpaRepository.countByLectureIdAndStatus(lectureId, ReviewStatus.ACTIVE);

        // 수강평 통계 DTO
        return new ReviewStats(
                averageRating,
                reviewCount
        );
    }

    @Override
    public Map<Long, ReviewStats> getReviewStatsMap(List<Long> lectureIds) {

        // 강의 ID 목록이 없다면 빈 Map 반환
        if (lectureIds == null || lectureIds.isEmpty()) {
            return Map.of();
        }

        // 강의 ID 목록을 리뷰 통계를 한 번에 조회
        return reviewJpaRepository.findReviewStatsByLectureIds(lectureIds).stream()
                .collect(Collectors.toMap(
                        ReviewJpaRepository.ReviewStatsProjection::getLectureId,
                        stats -> new ReviewStats(
                                stats.getAverageRating(),
                                stats.getReviewCount().intValue()
                        )
                ));
    }

    @Override
    public double getAverageRatingByLectureIds(List<Long> lectureIds) {
        // ACTIVE 강의가 하나도 없으면 잘못된 IN 쿼리를 실행하지 않고 0.0을 반환
        if (lectureIds == null || lectureIds.isEmpty()) {
            return 0.0;
        }

        // Review Repository에 강의 ID 목록을 전달하여 ACTIVE 수강평의 전체 평균 조회
        return reviewJpaRepository.findAverageRatingByLectureIds(lectureIds);
    }
}
