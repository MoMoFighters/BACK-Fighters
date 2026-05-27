package com.wanted.momocity.enrollment.domain.exception;

// 수강신청할 수 없는 강의 상태일 때 사용하는 예외
public class InvalidEnrollmentLectureStatusException extends RuntimeException {

    public InvalidEnrollmentLectureStatusException(String message) {
        super(message);
    }
}