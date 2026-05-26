package com.wanted.momocity.auth.infrastructure.exception;

import io.jsonwebtoken.JwtException;

public class ExpiredJwtCustomException extends JwtException {

    public ExpiredJwtCustomException(String message) {
        super(message);
    }
}
