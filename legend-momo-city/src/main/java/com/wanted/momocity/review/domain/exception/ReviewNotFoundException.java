package com.wanted.momocity.review.domain.exception;

// 수강평을 찾지 못했을 때 예외
public class ReviewNotFoundException extends RuntimeException {
    public ReviewNotFoundException(String message) {
        super(message);
    }
}
