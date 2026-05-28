package com.wanted.momocity.global.presentation.api.common;

import com.wanted.momocity.global.domain.common.exception.DomainRuleViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.NoHandlerFoundException;

/* comment
    ApiExceptionHandler 는 안쪽 계층의 예외를 바깥 API 계약으로 변환하는 프레젠테이션 adapter 다.
    domain / application 은 HTTP 상태 코드를 모르고, 이 클래스가 외부 프로토콜 규약을 책임진다.
    <핸들링 정책>
    1. 비즈니스 규칙 위반 : 400 / 사용자에게 메시지 노출 OK
    2. 입력 검증 실패 : 400 / 어느 필드/파라미터인지 명시
    3. 인증/인가 실패 : 401 or 403 / 메시지는 표준 문구로 통일
    4. 리소스 미존재 : 404 / 요청 경로 명시
    5. 예상치 못한 오류 : 500 / 메시지 마스킹 + 서버 로그 기록
    <순서 정책>
    구체적인 예외 -> 추상적인 예외 (catch-all Exception 은 무조건 마지막)
 */

@RestControllerAdvice
public class ApiExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(ApiExceptionHandler.class);

    // === 1. 비즈니스 규칙 위반 (400) ===

    @ExceptionHandler(DomainRuleViolationException.class)
    public ResponseEntity<ApiErrorResponse> handleDomainRuleViolation(DomainRuleViolationException exception) {
        return ResponseEntity.badRequest()
                .body(ApiErrorResponse.of(
                        HttpStatus.BAD_REQUEST.value(),
                        ApiResponseCode.DOMAIN_RULE_VIOLATION,
                        exception.getMessage()
                ));
    }

    // === 2. 입력 검증 실패 (400) ===

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiErrorResponse> handleValidation(MethodArgumentNotValidException exception) {
        // 검증 오류 표현 방식은 프레젠테이션 규약이므로 여기서 조합한다.
        String message = exception.getBindingResult().getFieldErrors().stream()
                .findFirst()
                .map(error -> "" + error.getDefaultMessage())
                .orElse(ApiResponseMessage.VALIDATION_ERROR);
        return ResponseEntity.badRequest()
                .body(ApiErrorResponse.of(
                        HttpStatus.BAD_REQUEST.value(),
                        ApiResponseCode.VALIDATION_ERROR,
                        message
                ));
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ApiErrorResponse> handleTypeMismatch(MethodArgumentTypeMismatchException exception) {
        String message = String.format("'%s' 파라미터 값이 유효하지 않습니다.", exception.getName());
        return ResponseEntity.badRequest()
                .body(ApiErrorResponse.of(
                        HttpStatus.BAD_REQUEST.value(),
                        ApiResponseCode.VALIDATION_ERROR,
                        message
                ));
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiErrorResponse> handleNotReadable(HttpMessageNotReadableException exception) {
        return ResponseEntity.badRequest()
                .body(ApiErrorResponse.of(
                        HttpStatus.BAD_REQUEST.value(),
                        ApiResponseCode.VALIDATION_ERROR,
                        "요청 본문 형식이 올바르지 않습니다."
                ));
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ApiErrorResponse> handleMissingParam(MissingServletRequestParameterException exception) {
        String message = String.format("필수 파라미터 '%s' 가 누락되었습니다.", exception.getParameterName());
        return ResponseEntity.badRequest()
                .body(ApiErrorResponse.of(
                        HttpStatus.BAD_REQUEST.value(),
                        ApiResponseCode.VALIDATION_ERROR,
                        message
                ));
    }

    // === 3. 인증/인가 실패 (401 / 403) ===

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

    // === 4. 리소스 미존재 (404) ===

    @ExceptionHandler(NoHandlerFoundException.class)
    public ResponseEntity<ApiErrorResponse> handleNoHandler(NoHandlerFoundException exception) {
        String message = String.format("요청하신 경로 '%s' 를 찾을 수 없습니다.", exception.getRequestURL());
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ApiErrorResponse.of(
                        HttpStatus.NOT_FOUND.value(),
                        ApiResponseCode.NOT_FOUND,
                        message
                ));
    }

    // === 5. 예상치 못한 오류 (500, 마지막 안전망) ===

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
