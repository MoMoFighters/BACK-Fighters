package com.wanted.momocity.enrollment.domain.exception;

// 본인 Id로 본인 마을 조회했을 때 예외
public class BuildingSelfAccessException extends RuntimeException {
    public BuildingSelfAccessException(String message) {
        super(message);
    }
}
