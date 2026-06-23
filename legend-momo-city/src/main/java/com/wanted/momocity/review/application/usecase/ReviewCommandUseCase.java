package com.wanted.momocity.review.application.usecase;

import com.wanted.momocity.review.application.command.ReviewCommand;
import com.wanted.momocity.review.presentation.api.response.CreateReviewResponse;

public interface ReviewCommandUseCase {
    // 수강평 등록
    CreateReviewResponse createReview(
            ReviewCommand.CreateReviewCommand command
    );
}
