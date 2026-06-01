package com.wanted.momocity.auth.domain.exception;

import com.wanted.momocity.auth.infrastructure.exception.ExpiredJwtCustomException;
import com.wanted.momocity.auth.infrastructure.exception.InvalidJwtCustomException;
import com.wanted.momocity.auth.infrastructure.exception.InvalidRefreshTokenException;
import com.wanted.momocity.global.presentation.api.common.ApiErrorResponse;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;

@Order(-1)
@RestControllerAdvice
public class AuthExceptionHandler {

    // 로그인 시 비밀번호 틀렸을 때
    @ExceptionHandler(InvalidCredentialsException.class)
    public ResponseEntity<ApiErrorResponse> handleInvalidCredentials(InvalidCredentialsException e) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(ApiErrorResponse.of(
                        HttpStatus.UNAUTHORIZED.value(),
                        "INVALID_CREDENTIALS",
                        e.getMessage()
                ));
    }

    // status가 active가 아닌 사용자가 로그인하려고 할 때
    @ExceptionHandler(InactiveUserException.class)
    public ResponseEntity<ApiErrorResponse> handleInactiveUser(InactiveUserException e) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(ApiErrorResponse.of(
                        HttpStatus.UNAUTHORIZED.value(),
                        e.getStatus(),
                        e.getMessage()
                ));
    }

    // 인증코드 값 일치하지 않을 때
    @ExceptionHandler(InvalidVerificationCodeException.class)
    public ResponseEntity<ApiErrorResponse> handleInvalidVerificationCode(InvalidVerificationCodeException e) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiErrorResponse.of(
                        HttpStatus.BAD_REQUEST.value(),
                        "INVALID_VERIFICATION_CODE",
                        e.getMessage()
                ));
    }

    // 이메일 중복일 때
    @ExceptionHandler(DuplicateEmailException.class)
    public ResponseEntity<ApiErrorResponse> handleDuplicateEmail(DuplicateEmailException e) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(ApiErrorResponse.of(
                        HttpStatus.CONFLICT.value(),
                        "DUPLICATE_EMAIL",
                        e.getMessage()
                ));
    }

    // 이메일 전송 실패했을 때
    @ExceptionHandler(EmailSendException.class)
    public ResponseEntity<ApiErrorResponse> handleEmailSend(EmailSendException e) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiErrorResponse.of(
                        HttpStatus.INTERNAL_SERVER_ERROR.value(),
                        "EMAIL_SEND_FAILED",
                        e.getMessage()
                ));
    }

    // 이메일 인증 안 했을 때
    @ExceptionHandler(EmailNotVerifiedException.class)
    public ResponseEntity<ApiErrorResponse> handleEmailNotVerified(EmailNotVerifiedException e) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(ApiErrorResponse.of(
                        HttpStatus.FORBIDDEN.value(),
                        "EMAIL_NOT_VERIFIED",
                        e.getMessage()
                ));
    }

    // 사용자를 찾지 못했을 때
    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<ApiErrorResponse> handleUserNotFoundException(UserNotFoundException e) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ApiErrorResponse.of(
                        HttpStatus.NOT_FOUND.value(),
                        "USER_NOT_FOUND",
                        e.getMessage()
                ));
    }

    // 임시 비밀번호 만료 시
    @ExceptionHandler(TempPasswordExpiredException.class)
    public ResponseEntity<ApiErrorResponse> handleTempPasswordExpired(TempPasswordExpiredException e) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(ApiErrorResponse.of(
                        HttpStatus.UNAUTHORIZED.value(),
                        "TEMP_PASSWORD_EXPIRED",
                        e.getMessage()
                ));
    }

    // 소셜 로그인 인가코드 만료됐거나 유효하지 않을 때
    @ExceptionHandler(OAuthInvalidCodeException.class)
    public ResponseEntity<ApiErrorResponse> handleOAuthInvalidCode(OAuthInvalidCodeException e) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiErrorResponse.of(
                        HttpStatus.BAD_REQUEST.value(),
                        "OAUTH_INVALID_CODE",
                        e.getMessage()
                ));
    }

    // 소셜 로그인 토큰 발급 / 유저정보 조회 실패
    @ExceptionHandler(OAuthTokenException.class)
    public ResponseEntity<ApiErrorResponse> handleOAuthToken(OAuthTokenException e) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiErrorResponse.of(
                        HttpStatus.INTERNAL_SERVER_ERROR.value(),
                        "OAUTH_TOKEN_ERROR",
                        e.getMessage()
                ));
    }

    // 리프레시 토큰이 없거나 유효하지 않을 때
    @ExceptionHandler(InvalidRefreshTokenException.class)
    public ResponseEntity<ApiErrorResponse> handleInvalidRefreshToken(InvalidRefreshTokenException e) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(ApiErrorResponse.of(
                        HttpStatus.UNAUTHORIZED.value(),
                        "INVALID_REFRESH_TOKEN",
                        e.getMessage()
                ));
    }

    // 잘못된 형식의 JWT 토큰일 때
    @ExceptionHandler(InvalidJwtCustomException.class)
    public ResponseEntity<ApiErrorResponse> handleInvalidJwt(InvalidJwtCustomException e) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(ApiErrorResponse.of(
                        HttpStatus.UNAUTHORIZED.value(),
                        "INVALID_JWT",
                        "유효하지 않은 토큰입니다. 다시 로그인해주세요."
                ));
    }

    // 리프레시 토큰 만료됐을 때
    @ExceptionHandler(ExpiredJwtCustomException.class)
    public ResponseEntity<ApiErrorResponse> handleExpiredJwt(ExpiredJwtCustomException e) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(ApiErrorResponse.of(
                        HttpStatus.UNAUTHORIZED.value(),
                        "EXPIRED_JWT",
                        "로그인이 만료되었습니다. 다시 로그인해주세요."
                ));
    }

    // 토큰이 없는 경우
    @ExceptionHandler(MissingTokenException.class)
    public ResponseEntity<ApiErrorResponse> handleMissingToken(MissingTokenException e) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(ApiErrorResponse.of(
                        HttpStatus.UNAUTHORIZED.value(),
                        "MISSING_TOKEN",
                        e.getMessage()
                ));
    }

    // 강사 증빙자료 누락
    @ExceptionHandler(MissingProofException.class)
    public ResponseEntity<ApiErrorResponse> handleMissingProof(MissingProofException e) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiErrorResponse.of(
                        HttpStatus.BAD_REQUEST.value(),
                        "MISSING_PROOF",
                        e.getMessage()
                ));
    }

}
