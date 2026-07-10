package com.wanted.momocity.review.domain.repository;

import com.wanted.momocity.review.domain.model.Review;
import com.wanted.momocity.review.domain.model.ReviewStatus;

import java.util.List;

// 수강평 저장소
public interface ReviewRepository {

    // 수강평을 저장
    Review save(Review review);

    // 특정 사용자가 특정 강의에 이미 수강평을 작성했는지 확인
    boolean existsByUserIdAndLectureIdAndStatus(
            Long userId,
            Long lectureId,
            ReviewStatus status
    );

    // 삭제 전 리뷰 존재 여부 확인
    boolean existsById(Long reviewId);

    // 리뷰 Id 기준 삭제
    void softDeleteById(Long reviewId);

    // lectureId 기준으로 해당 강의의 Active 수강평 원문 목록 조회
    List<String> findContentsByLectureId(Long lectureId);
}
