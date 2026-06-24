package com.wanted.momocity.review.application.usecase;

import com.wanted.momocity.review.application.query.ReviewQuery;
import com.wanted.momocity.review.presentation.api.response.ReviewListResponse;

// 수강평 조회 기능
public interface ReviewQueryUseCase {
    // 수강평 목록 조회 기능 메서드
    ReviewListResponse getReviews(
            ReviewQuery.GetReviewListQuery query
    );
}
