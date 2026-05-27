package com.wanted.momocity.auth.domain.exception;

public class OAuthInvalidCodeException extends RuntimeException {
    public OAuthInvalidCodeException(String message) {
        super(message);
    }
}
