package com.wanted.momocity.order.domain.exception;

public class AlreadyOwnedException extends RuntimeException {
    public AlreadyOwnedException(String message) {
        super(message);
    }
}
