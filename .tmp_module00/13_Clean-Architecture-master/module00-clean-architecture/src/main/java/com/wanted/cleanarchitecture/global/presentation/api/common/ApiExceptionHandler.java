package com.wanted.cleanarchitecture.global.presentation.api.common;

import com.wanted.cleanarchitecture.global.domain.common.exception.DomainRuleViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/*
 * ApiExceptionHandler는 안쪽 계층의 예외를 바깥 API 계약으로 변환하는 presentation adapter다.
 * domain/application은 HTTP 상태 코드를 모르고, 이 클래스가 외부 프로토콜 규약을 책임진다.
 */
@RestControllerAdvice
public class ApiExceptionHandler {

    @ExceptionHandler(DomainRuleViolationException.class)
    public ResponseEntity<ApiErrorResponse> handleDomainRuleViolation(DomainRuleViolationException exception) {
        return ResponseEntity.badRequest()
                .body(ApiErrorResponse.of(
                        HttpStatus.BAD_REQUEST.value(),
                        ApiResponseCode.DOMAIN_RULE_VIOLATION,
                        exception.getMessage()
                ));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiErrorResponse> handleValidation(MethodArgumentNotValidException exception) {
        // 검증 오류 표현 방식은 프레젠테이션 규약이므로 여기서 조합한다.
        String message = exception.getBindingResult().getFieldErrors().stream()
                .findFirst()
                .map(error -> error.getField() + " " + error.getDefaultMessage())
                .orElse(ApiResponseMessage.VALIDATION_ERROR);
        return ResponseEntity.badRequest()
                .body(ApiErrorResponse.of(
                        HttpStatus.BAD_REQUEST.value(),
                        ApiResponseCode.VALIDATION_ERROR,
                        message
                ));
    }
}
