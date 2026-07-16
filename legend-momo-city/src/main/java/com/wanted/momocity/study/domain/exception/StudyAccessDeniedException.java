package com.wanted.momocity.study.domain.exception;

/*
 * comment.
 *  Study 컨텍스트 전용 403 예외
 *  - 방장이 아닌데 강퇴 시도, 방 참가자가 아닌데 조회/타이머 액션 시도 등
 * */

public class StudyAccessDeniedException extends RuntimeException {
    public StudyAccessDeniedException(String message) {
        super(message);
    }
}