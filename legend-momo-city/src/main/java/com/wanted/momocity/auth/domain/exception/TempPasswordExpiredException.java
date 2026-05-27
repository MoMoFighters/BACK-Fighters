package com.wanted.momocity.auth.domain.exception;

public class TempPasswordExpiredException extends RuntimeException {
    public TempPasswordExpiredException(String message) {
        super(message);
    }
}
