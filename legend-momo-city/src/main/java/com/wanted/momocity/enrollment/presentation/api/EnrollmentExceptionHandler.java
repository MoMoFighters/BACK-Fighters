package com.wanted.momocity.enrollment.presentation.api;

import com.wanted.momocity.enrollment.domain.exception.*;
import com.wanted.momocity.global.presentation.api.common.ApiErrorResponse;
import com.wanted.momocity.global.presentation.api.common.ApiResponseCode;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

// enrollment 패키지에서 발생하는 예외를 API 응답으로 바꿔주는 클래스
// @RestControllerAdvice : enrollment 패키지에서 발생한 예외만 잡아서 API 응답 형태로 바꿔주는 예외 처리기
@RestControllerAdvice(basePackages = "com.wanted.momocity.enrollment")
public class EnrollmentExceptionHandler {

    // 중복 수강신청 예외를 409 Conflict로 변환
    @ExceptionHandler(DuplicateEnrollmentException.class)
    public ResponseEntity<ApiErrorResponse> handleDuplicateEnrollment(
            DuplicateEnrollmentException exception
    ) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(ApiErrorResponse.of(
                        HttpStatus.CONFLICT.value(),
                        ApiResponseCode.DOMAIN_RULE_VIOLATION,
                        exception.getMessage()
                ));
    }

    // 404 Error
    @ExceptionHandler(EnrollmentLectureNotFoundException.class)
    public ResponseEntity<ApiErrorResponse> handleLectureNotFound(
            EnrollmentLectureNotFoundException exception
    ) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ApiErrorResponse.of(
                        HttpStatus.NOT_FOUND.value(),
                        ApiResponseCode.NOT_FOUND,
                        exception.getMessage()
                ));
    }

    // 400 Error
    @ExceptionHandler(InvalidEnrollmentLectureStatusException.class)
    public ResponseEntity<ApiErrorResponse> handleInvalidLectureStatus(
            InvalidEnrollmentLectureStatusException exception
    ) {
        return ResponseEntity.badRequest()
                .body(ApiErrorResponse.of(
                        HttpStatus.BAD_REQUEST.value(),
                        ApiResponseCode.DOMAIN_RULE_VIOLATION,
                        exception.getMessage()
                ));
    }

    @ExceptionHandler(BuildingSelfAccessException.class)
    public ResponseEntity<ApiErrorResponse> handleBuildingSelfAccess(
            BuildingSelfAccessException exception
    ) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiErrorResponse.of(
                        HttpStatus.BAD_REQUEST.value(),
                        "BUILDING_SELF_ACCESS",
                        exception.getMessage()
                ));
    }

    // BASIC 회원의 수강신청 시도를 403 Forbidden 응답으로 변환
    @ExceptionHandler(BasicMembershipEnrollmentNotAllowedException.class)
    public ResponseEntity<ApiErrorResponse> handleBasicMembershipEnrollmentNotAllowed(
            // 서비스에서 발생한 BASIC 멤버십 제한 예외를 전달
            BasicMembershipEnrollmentNotAllowedException exception
    ) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(ApiErrorResponse.of(
                        HttpStatus.FORBIDDEN.value(),
                        ApiResponseCode.FORBIDDEN,
                        exception.getMessage()
                ));
    }
}