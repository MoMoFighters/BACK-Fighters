package com.wanted.momocity.lecture.domain.exception;

// 한 강의에 등록 가능한 챕터 개수를 초과했을 때 사용하는 예외
public class ChapterLimitExceededException extends RuntimeException {

    public ChapterLimitExceededException(String message) {
        super(message);
    }
}