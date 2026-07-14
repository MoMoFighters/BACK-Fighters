package com.wanted.momocity.study.domain.exception;

public class StudyNotFoundException extends RuntimeException {
    public StudyNotFoundException(String message) {
        super(message);
    }
}
