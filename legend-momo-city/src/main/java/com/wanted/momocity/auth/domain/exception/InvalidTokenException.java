package com.wanted.momocity.auth.domain.exception;

import com.wanted.momocity.global.domain.common.exception.DomainRuleViolationException;

public class InvalidTokenException extends DomainRuleViolationException {
    public InvalidTokenException(String message) {
        super(message);
    }
}
