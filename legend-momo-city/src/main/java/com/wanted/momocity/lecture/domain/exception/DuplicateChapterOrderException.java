package com.wanted.momocity.lecture.domain.exception;

// 같은 강의 안에서 챕터 순서가 중복될 때 사용하는 예외
public class DuplicateChapterOrderException extends RuntimeException {

    public DuplicateChapterOrderException(String message) {
        super(message);
    }
}