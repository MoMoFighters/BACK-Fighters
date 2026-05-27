package com.wanted.momocity.enrollment.domain.exception;

// 이미 수강신청한 강의를 다시 신청할 때 사용하는 예외
public class DuplicateEnrollmentException extends RuntimeException {

    public DuplicateEnrollmentException(String message) {
        super(message);
    }
}