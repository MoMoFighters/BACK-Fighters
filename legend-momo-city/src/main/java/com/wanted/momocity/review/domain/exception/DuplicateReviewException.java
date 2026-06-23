package com.wanted.momocity.review.domain.exception;

public class DuplicateReviewException extends RuntimeException {

    // 해당 강의에 강의평을 이미 작성한 경우 예외
    public DuplicateReviewException(String message) {
        super(message);
    }
}
