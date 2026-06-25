package com.wanted.momocity.order.domain.exception;

import com.wanted.momocity.global.presentation.api.common.ApiErrorResponse;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Order(0)
@RestControllerAdvice
public class OrderExceptionHandler {

    // 상품을 찾지 못했을 때
    @ExceptionHandler(ItemNotFoundException.class)
    public ResponseEntity<ApiErrorResponse> handleItemNotFoundException(ItemNotFoundException e) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ApiErrorResponse.of(
                        HttpStatus.NOT_FOUND.value(),
                        "ITEM_NOT_FOUND",
                        e.getMessage()
                ));
    }

    // 포인트 부족 시
    @ExceptionHandler(InsufficientPointException.class)
    public ResponseEntity<ApiErrorResponse> handleInsufficientPoint(InsufficientPointException e) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiErrorResponse.of(
                        HttpStatus.BAD_REQUEST.value(),
                        "INSUFFICIENT_POINT",
                        e.getMessage()
                ));
    }

    // 이미 보유한 상품 구매 시
    @ExceptionHandler(AlreadyOwnedException.class)
    public ResponseEntity<ApiErrorResponse> handleAlreadyOwned(AlreadyOwnedException e) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(ApiErrorResponse.of(
                        HttpStatus.CONFLICT.value(),
                        "ALREADY_OWNED",
                        e.getMessage()
                ));
    }
}
