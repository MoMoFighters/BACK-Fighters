package com.wanted.momocity.review.domain.exception;

public class ReviewAccessDeniedException extends RuntimeException {

    // 신청하지 않은 강의에서 리뷰 작성 시 예외 처리
    public ReviewAccessDeniedException(String message) {
        super(message);
    }
}
