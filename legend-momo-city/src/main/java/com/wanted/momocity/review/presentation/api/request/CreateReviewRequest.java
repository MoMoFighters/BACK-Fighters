package com.wanted.momocity.review.presentation.api.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

public record CreateReviewRequest(
        // 별점은 1점 이상
        @Min(value = 1, message = "별점은 1점 이상이어야 합니다.")
        // 별점은 5점 이하
        @Max(value = 5, message = "별점은 5점 이하여야 합니다.")
        int rating,

        // 수강평 내용은 비어있는거 방지
        @NotBlank(message = "수강평 내용은 필수입니다.")
        String content

) {
}
