package com.wanted.momocity.review.presentation.api;

import com.wanted.momocity.global.presentation.api.common.ApiErrorResponse;
import com.wanted.momocity.global.presentation.api.common.ApiResponseCode;
import com.wanted.momocity.lecture.domain.exception.LectureNotFoundException;
import com.wanted.momocity.review.domain.exception.DuplicateReviewException;
import com.wanted.momocity.review.domain.exception.ReviewAccessDeniedException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice(basePackages = "com.wanted.momocity.review")
public class ReviewExceptionHandler {

    // 이미 수강평 작성 409 응답
    @ExceptionHandler(DuplicateReviewException.class)
    public ResponseEntity<ApiErrorResponse> handleDuplicateReview(
            DuplicateReviewException exception
    ) {
        // 409 코드와 예외 메세지 포함해서 반환
        return  ResponseEntity.status(HttpStatus.CONFLICT)
                .body(ApiErrorResponse.of(
                        HttpStatus.CONFLICT.value(),
                        "REVIEW-DUPLICATE",
                        exception.getMessage()
                ));
    }

    // 수강평 작성 권한이 없는 경우 403 Forbidden으로 응답합니다.
    @ExceptionHandler(ReviewAccessDeniedException.class)
    public ResponseEntity<ApiErrorResponse> handleReviewAccessDenied(
            ReviewAccessDeniedException exception
    ) {
        // 403 상태 코드와 예외 메시지를 담아 반환합니다.
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(ApiErrorResponse.of(
                        HttpStatus.FORBIDDEN.value(),
                        ApiResponseCode.FORBIDDEN,
                        exception.getMessage()
                ));
    }

    // 강의를 찾을 수 없는 경우 404 Not Found로 응답합니다.
    @ExceptionHandler(LectureNotFoundException.class)
    public ResponseEntity<ApiErrorResponse> handleLectureNotFound(
            LectureNotFoundException exception
    ) {
        // 404 상태 코드와 예외 메시지를 담아 반환합니다.
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ApiErrorResponse.of(
                        HttpStatus.NOT_FOUND.value(),
                        ApiResponseCode.NOT_FOUND,
                        exception.getMessage()
                ));
    }
}
