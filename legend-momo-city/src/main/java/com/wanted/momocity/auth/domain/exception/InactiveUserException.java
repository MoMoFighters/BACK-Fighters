package com.wanted.momocity.auth.domain.exception;

import com.wanted.momocity.auth.domain.model.Status;

public class InactiveUserException extends RuntimeException {
    private final Status status;


    public InactiveUserException(String message, Status status) {
        super(message);
        this.status = status;
    }
    public String getStatus() {
        return status.name();
    }
}
