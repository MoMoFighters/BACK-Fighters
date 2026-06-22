package com.wanted.momocity.user.domain.exception;

import com.wanted.momocity.global.presentation.api.common.ApiErrorResponse;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Order(0)
@RestControllerAdvice
public class UserExceptionHandler {

    @ExceptionHandler(NicknameDuplicateException.class)
    public ResponseEntity<ApiErrorResponse> handleNicknameDuplicate(NicknameDuplicateException e) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(ApiErrorResponse.of(
                        HttpStatus.CONFLICT.value(),
                        "DUPLICATE_NICKNAME",
                        e.getMessage()
                ));
    }


    @ExceptionHandler(InvalidPasswordException.class)
    public ResponseEntity<ApiErrorResponse> handleInvalidPassword(InvalidPasswordException e) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiErrorResponse.of(
                        HttpStatus.BAD_REQUEST.value(),
                        "INVALID_PASSWORD",
                        e.getMessage()
                ));
    }

    @ExceptionHandler(SamePasswordException.class)
    public ResponseEntity<ApiErrorResponse> handleSamePassword(SamePasswordException e) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiErrorResponse.of(
                        HttpStatus.BAD_REQUEST.value(),
                        "SAME_PASSWORD",
                        e.getMessage()
                ));
    }

    @ExceptionHandler(InvalidReasonException.class)
    public ResponseEntity<ApiErrorResponse> handleInvalidReason(InvalidReasonException e) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiErrorResponse.of(
                        HttpStatus.BAD_REQUEST.value(),
                        "INVALID_REASON",
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

    // 증빙자료 누락 시
    @ExceptionHandler(MissingProofException.class)
    public ResponseEntity<ApiErrorResponse> handleMissingProof(MissingProofException e) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiErrorResponse.of(
                        HttpStatus.BAD_REQUEST.value(),
                        "MISSING_PROOF",
                        e.getMessage()
                ));
    }

    // 증빙 자료 확장자 안 맞을 시
    @ExceptionHandler(InvalidFileExtensionException.class)
    public ResponseEntity<ApiErrorResponse> handleInvalidFileExtension(InvalidFileExtensionException e) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiErrorResponse.of(
                        HttpStatus.BAD_REQUEST.value(),
                        "INVALID_FILE_EXTENSION",
                        e.getMessage()
                ));
    }

}