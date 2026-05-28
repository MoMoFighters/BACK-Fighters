package com.wanted.momocity.lecture.domain.exception;

// 강의를 찾을 수 없을 때 사용하는 예외
public class LectureNotFoundException extends RuntimeException {

    public LectureNotFoundException(String message) {
        super(message);
    }
}