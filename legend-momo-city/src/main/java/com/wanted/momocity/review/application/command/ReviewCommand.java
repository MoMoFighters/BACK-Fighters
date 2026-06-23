package com.wanted.momocity.review.application.command;

public final class ReviewCommand {
    private ReviewCommand() {
    }

    public record CreateReviewCommand(
        Long lectureId,
        Long userId,
        int rating,
        String content
    ) {}
}
