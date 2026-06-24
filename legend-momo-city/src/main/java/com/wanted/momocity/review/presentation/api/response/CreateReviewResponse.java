package com.wanted.momocity.review.presentation.api.response;

import com.wanted.momocity.review.domain.model.Review;

import java.time.LocalDateTime;

// 수강평 등록 성공 읍답
public record CreateReviewResponse(

        Long reviewId,

        Long lectureId,

        Long userId,

        int rating,

        String content,

        LocalDateTime createdAt
) {
    public static CreateReviewResponse from(Review review) {
        return new CreateReviewResponse(
                review.getId(),
                review.getLectureId(),
                review.getUserId(),
                review.getRating(),
                review.getContent(),
                review.getCreatedAt()
        );
    }
}
