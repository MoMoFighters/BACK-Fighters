package com.wanted.momocity.payment.domain.exception;

import com.wanted.momocity.global.presentation.api.common.ApiErrorResponse;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Order(0)
@RestControllerAdvice
public class PaymentExceptionHandler {
    @ExceptionHandler(PaymentSamePlanException.class)

    // 현재와 같은 구독 플랜으로 결제하려고 하는 경우
    public ResponseEntity<ApiErrorResponse> handleSamePlan(PaymentSamePlanException e) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(ApiErrorResponse.of(
                        HttpStatus.CONFLICT.value(),
                        "PAYMENT_SAME_PLAN",
                        e.getMessage()
                ));
    }

    // 플랜 다운그레이드 시에는 결제 안 함
    @ExceptionHandler(PaymentDowngradeNotAllowedException.class)
    public ResponseEntity<ApiErrorResponse> handleDowngradeNotAllowed(PaymentDowngradeNotAllowedException e) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiErrorResponse.of(
                        HttpStatus.BAD_REQUEST.value(),
                        "PAYMENT_DOWNGRADE_NOT_ALLOWED",
                        e.getMessage()
                ));
    }

    // 이미 결제 진행 중인데 또 결제 버튼 눌렀을 떄
    @ExceptionHandler(PaymentAlreadyInProgressException.class)
    public ResponseEntity<ApiErrorResponse> handleAlreadyInProgress(PaymentAlreadyInProgressException e) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(ApiErrorResponse.of(
                        HttpStatus.CONFLICT.value(),
                        "PAYMENT_ALREADY_IN_PROGRESS",
                        e.getMessage()
                ));
    }
}
