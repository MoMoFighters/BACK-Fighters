package com.wanted.momocity.review.domain.repository;

import com.wanted.momocity.review.domain.model.Review;

// 수강평 저장소
public interface ReviewRepository {

    // 수강평을 저장
    Review save(Review review);

    // 특정 사용자가 특정 강의에 이미 수강평을 작성했는지 확인
    boolean existsByUserIdAndLectureId(
            Long userId,
            Long lectureId
    );
}
