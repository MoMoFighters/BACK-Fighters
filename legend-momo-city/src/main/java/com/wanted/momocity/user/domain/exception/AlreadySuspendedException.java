package com.wanted.momocity.user.domain.exception;

public class AlreadySuspendedException extends RuntimeException {
    public AlreadySuspendedException(String message) {
        super(message);
    }
}
