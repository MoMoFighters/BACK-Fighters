package com.wanted.momocity.global.presentation.api.common;

import com.wanted.momocity.global.domain.common.exception.DomainRuleViolationException;
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
public class ApiExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(ApiExceptionHandler.class);

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

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ApiErrorResponse> handleAuthentication(AuthenticationException exception) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(ApiErrorResponse.of(
                        HttpStatus.UNAUTHORIZED.value(),
                        ApiResponseCode.UNAUTHORIZED,
                        ApiResponseMessage.UNAUTHORIZED
                ));
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiErrorResponse> handleAccessDenied(AccessDeniedException exception) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(ApiErrorResponse.of(
                        HttpStatus.FORBIDDEN.value(),
                        ApiResponseCode.FORBIDDEN,
                        ApiResponseMessage.FORBIDDEN
                ));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiErrorResponse> handleUnexpected(Exception exception) {
        // 마지막 안전망. 내부 예외 메시지는 클라이언트에 노출하지 않고 서버 로그에만 남긴다.
        log.error("[Unexpected] {}", exception.getMessage(), exception);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiErrorResponse.of(
                        HttpStatus.INTERNAL_SERVER_ERROR.value(),
                        ApiResponseCode.INTERNAL_ERROR,
                        ApiResponseMessage.INTERNAL_ERROR
                ));
    }
}
