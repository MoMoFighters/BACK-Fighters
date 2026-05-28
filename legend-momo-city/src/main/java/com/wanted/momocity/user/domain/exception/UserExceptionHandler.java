package com.wanted.momocity.user.domain.exception;

import com.wanted.momocity.global.presentation.api.common.ApiErrorResponse;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Order(0)
@RestControllerAdvice
public class UserExceptionHandler {

    @ExceptionHandler(NicknameDuplicateException.class)
    public ResponseEntity<ApiErrorResponse> handleNicknameDuplicate(NicknameDuplicateException e) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(ApiErrorResponse.of(
                        HttpStatus.CONFLICT.value(),
                        "DUPLICATE_NICKNAME",
                        e.getMessage()
                ));
    }
}