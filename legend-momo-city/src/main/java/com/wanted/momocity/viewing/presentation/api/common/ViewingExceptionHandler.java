package com.wanted.momocity.viewing.presentation.api.common;

import com.wanted.momocity.global.presentation.api.common.ApiErrorResponse;
import com.wanted.momocity.global.presentation.api.common.ApiResponseCode;
import com.wanted.momocity.viewing.domain.exception.ViewingAccessDeniedException;
import com.wanted.momocity.viewing.domain.exception.ViewingNotFoundException;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;


/*
* comment.
*  Viewing 컨텍스트 전용 예외 처리
*  global ApiExceptionHandler 를 건드리지 않고 viewing 예외만 독립적으로 처리
*/

@Order(1)
@RestControllerAdvice
public class ViewingExceptionHandler {

    // 403 권한 없음
    @ExceptionHandler(ViewingAccessDeniedException.class)
    public ResponseEntity<ApiErrorResponse> handleViewingAccessDenied(
            ViewingAccessDeniedException exception) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(ApiErrorResponse.of(
                        HttpStatus.FORBIDDEN.value(),
                        ApiResponseCode.FORBIDDEN,
                        exception.getMessage()
                ));
    }

    // 404 리소스 없음
    @ExceptionHandler(ViewingNotFoundException.class)
    public ResponseEntity<ApiErrorResponse> handleViewingNotFound(
            ViewingNotFoundException exception) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ApiErrorResponse.of(
                        HttpStatus.NOT_FOUND.value(),
                        ApiResponseCode.NOT_FOUND,
                        exception.getMessage()
                ));
    }

}
