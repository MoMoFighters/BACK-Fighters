package com.wanted.momocity.auth.domain.exception;

public class MissingProofException extends RuntimeException {
    public MissingProofException(String message) {
        super(message);
    }
}
