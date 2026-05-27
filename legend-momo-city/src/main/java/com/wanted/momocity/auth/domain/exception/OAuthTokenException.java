package com.wanted.momocity.auth.domain.exception;

public class OAuthTokenException extends RuntimeException {
    public OAuthTokenException(String message) {
        super(message);
    }
}
