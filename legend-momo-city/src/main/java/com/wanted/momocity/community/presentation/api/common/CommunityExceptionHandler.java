package com.wanted.momocity.community.presentation.api.common;

import com.wanted.momocity.community.domain.exception.CommunityAccessDeniedException;
import com.wanted.momocity.community.domain.exception.CommunityNotFoundException;
import com.wanted.momocity.global.presentation.api.common.ApiErrorResponse;
import com.wanted.momocity.global.presentation.api.common.ApiResponseCode;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/*
* comment.
*  community 컨텍스트 전용 예외 처리
*  -> global ApiExceptionHandler 건드리지 않고 community 예외만 독립적으로 처리
* */

@RestControllerAdvice
public class CommunityExceptionHandler {

    // 403 권한 없음
    @ExceptionHandler(CommunityAccessDeniedException.class)
    public ResponseEntity<ApiErrorResponse> handleCommunityAccessDenied(
            CommunityAccessDeniedException exception
    ) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(ApiErrorResponse.of(
                        HttpStatus.FORBIDDEN.value(),
                        ApiResponseCode.FORBIDDEN,
                        exception.getMessage()
                ));
    }

    // 404 리소스 없음
    @ExceptionHandler(CommunityNotFoundException.class)
    public ResponseEntity<ApiErrorResponse> handleCommunityNotFound(
            CommunityNotFoundException exception
    ) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ApiErrorResponse.of(
                        HttpStatus.NOT_FOUND.value(),
                        ApiResponseCode.NOT_FOUND,
                        exception.getMessage()
                ));
    }

}
