package com.wanted.momocity.study.domain.exception;

public class StudyAccessDeniedException extends RuntimeException {
    public StudyAccessDeniedException(String message) {
        super(message);
    }
}
