package com.wanted.momocity.streak.presentation.api.common;

import com.wanted.momocity.global.presentation.api.common.ApiErrorResponse;
import com.wanted.momocity.global.presentation.api.common.ApiResponseCode;
import jakarta.validation.ConstraintViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.DateTimeException;

/*
* comment.
*  Streak 컨텍스트 전용 예외 처리
*  -> 날짜 관련 예외는 streak 도메인에서만 사용하므로 여기서 처리
* */

@RestControllerAdvice
public class StreakExceptionHandler {

    // LocalDate.of(year, month, 1) 에서 잘못된 날짜 입력 시 발생
    // 500 대신 400 으로 처리
    @ExceptionHandler(DateTimeException.class)
    public ResponseEntity<ApiErrorResponse> handleDateTimeException(
            DateTimeException exception
    ) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiErrorResponse.of(
                        HttpStatus.BAD_REQUEST.value(),
                        ApiResponseCode.VALIDATION_ERROR,
                        "유효하지 않은 날짜 값입니다."
                ));
    }

    // @Min, @Max 검증 실패 시 발생 -> 400 으로 처리
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiErrorResponse> handleConstraintViolation(
            ConstraintViolationException exception
    ) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiErrorResponse.of(
                        HttpStatus.BAD_REQUEST.value(),
                        ApiResponseCode.VALIDATION_ERROR,
                        "year 는 2000~2100, month 는 1~12 사이 값이어야 합니다."
                ));
    }

}
