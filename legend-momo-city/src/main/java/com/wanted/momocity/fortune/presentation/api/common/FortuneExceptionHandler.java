package com.wanted.momocity.fortune.presentation.api.common;

import com.wanted.momocity.fortune.domain.exception.FortuneNotFoundException;
import com.wanted.momocity.fortune.domain.exception.InsufficientFortunePointException;
import com.wanted.momocity.global.presentation.api.common.ApiErrorResponse;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

// 전역 예외 처리기보다 운세 전용 처리기를 먼저 실행하도록 설정합니다.
@Order(Ordered.HIGHEST_PRECEDENCE)
// 운세 API에서 발생하는 예외를 공통으로 처리합니다.
@RestControllerAdvice
public class FortuneExceptionHandler {

    // 포인트가 부족한 예외를 처리하도록 지정합니다.
    @ExceptionHandler(InsufficientFortunePointException.class)
    public ResponseEntity<ApiErrorResponse> handleInsufficientPoint(
            InsufficientFortunePointException exception
    ) {
        // 포인트 부족은 요청 조건을 충족하지 못한 것이므로 400 응답을 반환합니다.
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                // 프로젝트 공통 에러 응답 형태로 본문을 생성합니다.
                .body(ApiErrorResponse.of(
                        // HTTP 상태 코드 숫자 400을 저장합니다.
                        HttpStatus.BAD_REQUEST.value(),
                        // 프론트가 포인트 부족 상황을 구분할 응답 코드입니다.
                        FortuneResponseCode.INSUFFICIENT_POINT,
                        // 사용자에게 보여줄 포인트 부족 메시지입니다.
                        FortuneResponseMessage.INSUFFICIENT_POINT
                ));
    }

    // 운세 원본 데이터가 없는 예외를 처리하도록 지정합니다.
    @ExceptionHandler(FortuneNotFoundException.class)
    public ResponseEntity<ApiErrorResponse> handleFortuneNotFound(
            FortuneNotFoundException exception
    ) {
        // 시드 데이터 누락이나 데이터 불일치에 해당하므로 500 응답을 반환합니다.
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                // 프로젝트 공통 에러 응답 형태로 본문을 생성합니다.
                .body(ApiErrorResponse.of(
                        // HTTP 상태 코드 숫자 500을 저장합니다.
                        HttpStatus.INTERNAL_SERVER_ERROR.value(),
                        // 운세 데이터 문제를 구분할 응답 코드입니다.
                        FortuneResponseCode.DATA_NOT_FOUND,
                        // 외부 사용자에게 전달할 운세 데이터 미존재 메시지입니다.
                        FortuneResponseMessage.DATA_NOT_FOUND
                ));
    }
}
