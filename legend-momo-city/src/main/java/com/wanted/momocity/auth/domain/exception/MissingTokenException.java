package com.wanted.momocity.auth.domain.exception;

import com.wanted.momocity.global.domain.common.exception.DomainRuleViolationException;

public class MissingTokenException extends DomainRuleViolationException {
    public MissingTokenException(String message) {
        super(message);
    }
}
