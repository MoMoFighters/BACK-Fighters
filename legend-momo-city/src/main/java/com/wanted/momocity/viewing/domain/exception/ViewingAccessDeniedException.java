package com.wanted.momocity.viewing.domain.exception;

public class ViewingAccessDeniedException extends RuntimeException {
    public ViewingAccessDeniedException(String message) {
        super(message);
    }
}
