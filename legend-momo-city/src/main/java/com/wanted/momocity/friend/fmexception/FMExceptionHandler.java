package com.wanted.momocity.friend.fmexception;

import com.wanted.momocity.global.domain.common.exception.DomainRuleViolationException;
import com.wanted.momocity.global.presentation.api.common.ApiErrorResponse;
import com.wanted.momocity.global.presentation.api.common.ApiResponseCode;
import com.wanted.momocity.global.presentation.api.common.ApiResponseMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/*
 * ApiExceptionHandler는 안쪽 계층의 예외를 바깥 API 계약으로 변환하는 presentation adapter다.
 * domain / application은 HTTP 상태 코드를 모르고, 이 클래스가 외부 프로토콜 규약을 책임진다.
 *
 * 핸들링 정책:
 * - 비즈니스 예외(DomainRuleViolation, Validation) → 사용자에게 메시지 노출 OK
 * - 인증/인가 예외(Authentication, AccessDenied)   → 메시지는 표준 문구로 통일
 * - 예상치 못한 예외(Exception)                      → 메시지 마스킹 + 서버 로그 기록
 */
@RestControllerAdvice
public class FMExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(FMExceptionHandler.class);

    //409 예외 핸들러
    @ExceptionHandler(FMResourceConflictException.class)
    public ResponseEntity<ApiErrorResponse> handlerResourceConflict(FMResourceConflictException exception) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(ApiErrorResponse.of(
                        HttpStatus.CONFLICT.value(),
                        "RESOURCE_CONFLICT",
                        exception.getMessage()
                ));
    }

    //404 예외 핸들러
    @ExceptionHandler(FMResourceNotFoundException.class)
    public ResponseEntity<ApiErrorResponse> handleResourceNotFound(FMResourceNotFoundException exception) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ApiErrorResponse.of(
                        HttpStatus.NOT_FOUND.value(),
                        "RESOURCE_NOT_FOUND",
                        exception.getMessage()
                ));
    }

    //403 예외 핸들러
    @ExceptionHandler(FMResourceAccessDeniedException.class)
    public ResponseEntity<ApiErrorResponse> handleResourceAccessDenied(FMResourceAccessDeniedException exception) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(ApiErrorResponse.of(
                        HttpStatus.FORBIDDEN.value(),
                        "ACCESS_DENIED",
                        exception.getMessage()
                ));
    }

    //400 예외 핸들러
    @ExceptionHandler(FMBusinessRuleViolationException.class)
    public ResponseEntity<ApiErrorResponse> handleBusinessRuleViolation(FMBusinessRuleViolationException exception) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiErrorResponse.of(
                        HttpStatus.BAD_REQUEST.value(),
                        "BAD_REQUEST",
                        exception.getMessage()
                ));
    }
}
