package com.wanted.momocity.review.application.usecase;

import com.wanted.momocity.review.application.command.ReviewCommand;

public interface ReviewCommandUseCase {
    // 수강평 등록
    void createReview(
            ReviewCommand.CreateReviewCommand command
    );
}
