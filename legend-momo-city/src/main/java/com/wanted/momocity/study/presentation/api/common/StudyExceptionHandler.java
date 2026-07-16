package com.wanted.momocity.study.presentation.api.common;

import com.wanted.momocity.global.presentation.api.common.ApiErrorResponse;
import com.wanted.momocity.global.presentation.api.common.ApiResponseCode;
import com.wanted.momocity.study.domain.exception.StudyAccessDeniedException;
import com.wanted.momocity.study.domain.exception.StudyNotFoundException;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/*
 * comment.
 *  study 컨텍스트 전용 예외 처리
 *  -> global ApiExceptionHandler 건드리지 않고 study 예외만 독립적으로 처리
 *  -> 400(DomainRuleViolationException)은 global이 처리하므로 여기서는 403/404만 다룸
 * */

@Order(Ordered.HIGHEST_PRECEDENCE)
@RestControllerAdvice
public class StudyExceptionHandler {

    // 403 권한 없음
    @ExceptionHandler(StudyAccessDeniedException.class)
    public ResponseEntity<ApiErrorResponse> handleStudyAccessDenied(
            StudyAccessDeniedException exception
    ) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(ApiErrorResponse.of(
                        HttpStatus.FORBIDDEN.value(),
                        ApiResponseCode.FORBIDDEN,
                        exception.getMessage()
                ));
    }

    // 404 리소스 없음
    @ExceptionHandler(StudyNotFoundException.class)
    public ResponseEntity<ApiErrorResponse> handleStudyNotFound(
            StudyNotFoundException exception
    ) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ApiErrorResponse.of(
                        HttpStatus.NOT_FOUND.value(),
                        ApiResponseCode.NOT_FOUND,
                        exception.getMessage()
                ));
    }

}
