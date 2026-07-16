package com.wanted.momocity.study.domain.exception;

/*
 * comment.
 *  Study 컨텍스트 전용 404 예외
 *  - 방, 초대, 세션 등 존재하지 않는 리소스 조회 시
 * */

public class StudyNotFoundException extends RuntimeException {
    public StudyNotFoundException(String message) {
        super(message);
    }
}