package com.wanted.momocity.user.domain.exception;

public class NicknameDuplicateException extends RuntimeException {
    public NicknameDuplicateException(String message) {
        super(message);
    }
}
