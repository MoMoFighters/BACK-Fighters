package com.wanted.momocity.review.domain.event;

public record ReviewCreatedEvent(
        Long reviewId,
        Long userId,
        Long lectureId
) {
}