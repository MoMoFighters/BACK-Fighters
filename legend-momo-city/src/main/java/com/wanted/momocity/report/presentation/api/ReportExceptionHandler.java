package com.wanted.momocity.report.presentation.api;

import com.wanted.momocity.global.presentation.api.common.ApiErrorResponse;
import com.wanted.momocity.global.presentation.api.common.ApiResponseCode;
import com.wanted.momocity.report.domain.exception.ReportNotFoundException;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

// report 패키지에서 발생하는 전용 예외를 API 응답으로 변환
// @Order(HIGHEST_PRECEDENCE) : 전역 ApiExceptionHandler 의 catch-all(Exception.class) 보다 먼저 처리되도록 우선순위 설정
@Order(Ordered.HIGHEST_PRECEDENCE)
@RestControllerAdvice(basePackages = "com.wanted.momocity.report")
public class ReportExceptionHandler {

    // 신고를 찾을 수 없는 경우 404로 응답
    @ExceptionHandler(ReportNotFoundException.class)
    public ResponseEntity<ApiErrorResponse> handleReportNotFound(
            ReportNotFoundException exception
    ) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ApiErrorResponse.of(
                        HttpStatus.NOT_FOUND.value(),
                        ApiResponseCode.NOT_FOUND,
                        exception.getMessage()
                ));
    }
}