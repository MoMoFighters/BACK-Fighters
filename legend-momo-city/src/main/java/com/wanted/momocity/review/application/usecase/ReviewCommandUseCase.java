package com.wanted.momocity.review.application.usecase;

import com.wanted.momocity.review.application.command.ReviewCommand;

public interface ReviewCommandUseCase {
    // 수강평 등록
    void createReview(
            ReviewCommand.CreateReviewCommand command
    );

    // 관리자 수강평 삭제 기능
    void deleteReview(Long reviewId);
}
