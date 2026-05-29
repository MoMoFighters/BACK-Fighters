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


    @ExceptionHandler(InvalidPasswordException.class)
    public ResponseEntity<ApiErrorResponse> handleInvalidPassword(InvalidPasswordException e) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiErrorResponse.of(
                        HttpStatus.BAD_REQUEST.value(),
                        "INVALID_PASSWORD",
                        e.getMessage()
                ));
    }

    @ExceptionHandler(SamePasswordException.class)
    public ResponseEntity<ApiErrorResponse> handleSamePassword(SamePasswordException e) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiErrorResponse.of(
                        HttpStatus.BAD_REQUEST.value(),
                        "SAME_PASSWORD",
                        e.getMessage()
                ));
    }

    @ExceptionHandler(InvalidReasonException.class)
    public ResponseEntity<ApiErrorResponse> handleInvalidReason(InvalidReasonException e) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiErrorResponse.of(
                        HttpStatus.BAD_REQUEST.value(),
                        "INVALID_REASON",
                        e.getMessage()
                ));
    }

}