package com.wanted.momocity.friend.fmexception;

//409 예외 처리
public class FMResourceConflictException extends RuntimeException {
    public FMResourceConflictException(String message) {
        super(message);
    }

    public FMResourceConflictException(String message, Throwable cause) {
        super(message, cause);
    }
}
