package com.wanted.momocity.fortune.domain.exception;

public class FortuneNotFoundException extends RuntimeException {
    public FortuneNotFoundException() {
        super("운세 데이터를 찾을 수 없습니다.");
    }
}
