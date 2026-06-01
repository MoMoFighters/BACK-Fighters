package com.wanted.momocity.lecture.presentation.api;

import com.wanted.momocity.global.presentation.api.common.ApiErrorResponse;
import com.wanted.momocity.global.presentation.api.common.ApiResponseCode;
import com.wanted.momocity.lecture.domain.exception.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

// lecture 패키지에서 발생하는 전용 예외를 API 응답으로 변환
@RestControllerAdvice(basePackages = "com.wanted.momocity.lecture")
public class LectureExceptionHandler {

    // 강의를 찾을 수 없는 경우 404로 응답
    @ExceptionHandler(LectureNotFoundException.class)
    public ResponseEntity<ApiErrorResponse> handleLectureNotFound(
            LectureNotFoundException exception
    ) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ApiErrorResponse.of(
                        HttpStatus.NOT_FOUND.value(),
                        ApiResponseCode.NOT_FOUND,
                        exception.getMessage()
                ));
    }

    // 챕터 순서가 중복된 경우 409로 응답
    @ExceptionHandler(DuplicateChapterOrderException.class)
    public ResponseEntity<ApiErrorResponse> handleDuplicateChapterOrder(
            DuplicateChapterOrderException exception
    ) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(ApiErrorResponse.of(
                        HttpStatus.CONFLICT.value(),
                        ApiResponseCode.DOMAIN_RULE_VIOLATION,
                        exception.getMessage()
                ));
    }

    // 챕터 최대 개수를 초과한 경우 409로 응답합니다.
    @ExceptionHandler(ChapterLimitExceededException.class)
    public ResponseEntity<ApiErrorResponse> handleChapterLimitExceeded(
            ChapterLimitExceededException exception
    ) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(ApiErrorResponse.of(
                        HttpStatus.CONFLICT.value(),
                        ApiResponseCode.DOMAIN_RULE_VIOLATION,
                        exception.getMessage()
                ));
    }

    // 챕터를 찾을 수 없을 때 404 응답을 반환
    @ExceptionHandler(ChapterNotFoundException.class)
    public ResponseEntity<ApiErrorResponse> handleChapterNotFound(ChapterNotFoundException exception) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ApiErrorResponse.of(
                        HttpStatus.NOT_FOUND.value(),
                        ApiResponseCode.NOT_FOUND,
                        exception.getMessage()
                ));
    }

    // 이미 동영상이 등록된 챕터에 다시 등록 요청이 들어오면 409 응답을 반환
    @ExceptionHandler(ChapterVideoAlreadyExistsException.class)
    public ResponseEntity<ApiErrorResponse> handleChapterVideoAlreadyExists(ChapterVideoAlreadyExistsException exception) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(ApiErrorResponse.of(
                        HttpStatus.CONFLICT.value(),
                        ApiResponseCode.DOMAIN_RULE_VIOLATION,
                        exception.getMessage()
                ));
    }
}