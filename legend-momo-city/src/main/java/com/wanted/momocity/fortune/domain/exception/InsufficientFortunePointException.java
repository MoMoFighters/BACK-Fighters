package com.wanted.momocity.fortune.domain.exception;

public class InsufficientFortunePointException extends RuntimeException {
    public InsufficientFortunePointException() {
        super("운세를 뽑기 위한 포인트가 부족합니다.");
    }
}
