package com.wanted.momocity.review.presentation.api.response;

import com.wanted.momocity.global.presentation.api.common.ApiResponseCode;

import java.time.LocalDateTime;

// data 필드가 없는 수강평 등록 성공 응답입니다.
public record CreateReviewSuccessResponse(
        LocalDateTime timestamp,
        int status,
        String code,
        String message
) {
    public static CreateReviewSuccessResponse created() {
        return new CreateReviewSuccessResponse(
                LocalDateTime.now(),
                201,
                ApiResponseCode.CREATED,
                "수강평이 등록되었습니다."
        );
    }

    public static CreateReviewSuccessResponse deleted() {
        return new CreateReviewSuccessResponse(
                LocalDateTime.now(),
                200,
                ApiResponseCode.SUCCESS,
                "수강평이 삭제되었습니다."
        );
    }
}