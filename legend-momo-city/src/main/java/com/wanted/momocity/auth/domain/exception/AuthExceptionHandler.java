package com.wanted.momocity.auth.domain.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;

@RestControllerAdvice
public class AuthExceptionHandler {

    // 로그인 시 비밀번호 틀렸을 때
    @ExceptionHandler(InvalidCredentialsException.class)
    public ResponseEntity<?> handleInvalidCredentials(InvalidCredentialsException e) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(Map.of("success", false, "message", e.getMessage()));
    }

    // status가 active가 아닌 사용자가 로그인하려고 할 때
    @ExceptionHandler(InactiveUserException.class)
    public ResponseEntity<?> handleInactiveUser(InactiveUserException e) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(Map.of("success", false, "message", e.getMessage()));
    }

    // 인증코드 값 일치하지 않을 때
    @ExceptionHandler(InvalidVerificationCodeException.class)
    public ResponseEntity<?> handleInvalidVerificationCode(InvalidVerificationCodeException e) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of("success", false, "message", e.getMessage()));
    }

    // 이메일 중복일 때
    @ExceptionHandler(DuplicateEmailException.class)
    public ResponseEntity<?> handleDuplicateEmail(DuplicateEmailException e) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(Map.of("success", false, "message", e.getMessage()));
    }

    // 이메일 전송 실패했을 때
    @ExceptionHandler(EmailSendException.class)
    public ResponseEntity<?> handleEmailSend(EmailSendException e) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("success", false, "message", e.getMessage()));
    }
}
