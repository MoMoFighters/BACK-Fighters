package com.wanted.momocity.enrollment.domain.exception;

// 수강신청하려는 강의가 없을 때 사용하는 예외
public class EnrollmentLectureNotFoundException extends RuntimeException {

    public EnrollmentLectureNotFoundException(String message) {
        super(message);
    }
}