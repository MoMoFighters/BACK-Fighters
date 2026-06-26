package com.wanted.momocity.store.domain.exception;

import com.wanted.momocity.global.presentation.api.common.ApiErrorResponse;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Order(0)
@RestControllerAdvice
public class StoreExceptionHandler {

    // 상품을 찾지 못했을 때
    @ExceptionHandler(ItemNotFoundException.class)
    public ResponseEntity<ApiErrorResponse> handleItemNotFoundException(ItemNotFoundException e) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ApiErrorResponse.of(
                        HttpStatus.NOT_FOUND.value(),
                        "STORE_ITEM_NOT_FOUND",
                        e.getMessage()
                ));
    }

    // 소유하지 않은 상품을 장착하려고 할 때
    @ExceptionHandler(ItemNotOwnedException.class)
    public ResponseEntity<ApiErrorResponse> handleItemNotOwnedException(ItemNotOwnedException e) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(ApiErrorResponse.of(
                        HttpStatus.FORBIDDEN.value(),
                        "STORE_ITEM_NOT_OWNED",
                        e.getMessage()
                ));
    }
}
