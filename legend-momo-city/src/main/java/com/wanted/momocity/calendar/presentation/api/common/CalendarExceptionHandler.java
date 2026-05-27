package com.wanted.momocity.calendar.presentation.api.common;

import com.wanted.momocity.calendar.domain.exception.CalendarAccessDeniedException;
import com.wanted.momocity.calendar.domain.exception.CalendarNotFoundException;
import com.wanted.momocity.global.presentation.api.common.ApiErrorResponse;
import com.wanted.momocity.global.presentation.api.common.ApiResponseCode;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/*
* comment.
*  calendar 컨텍스트 전용 예외 처리
*  global ApiExceptionHandler 를 건드리지 않고 calendar 예외만 독립적으로 처리
* */

@RestControllerAdvice
public class CalendarExceptionHandler {

    // 403 권한 없음
    @ExceptionHandler(CalendarAccessDeniedException.class)
    public ResponseEntity<ApiErrorResponse> handleCalendarAccessDenied(
            CalendarAccessDeniedException exception
    ) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(ApiErrorResponse.of(
                        HttpStatus.FORBIDDEN.value(),
                        ApiResponseCode.FORBIDDEN,
                        exception.getMessage()
                ));
    }

    //404 리소스 없음
    @ExceptionHandler(CalendarNotFoundException.class)
    public ResponseEntity<ApiErrorResponse> handleCalendarNorFound(
            CalendarNotFoundException exception
    ) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ApiErrorResponse.of(
                        HttpStatus.NOT_FOUND.value(),
                        ApiResponseCode.NOT_FOUND,
                        exception.getMessage()
                ));
    }

}
