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

    // 결제 정보를 찾을 수 없는 경우
    @ExceptionHandler(PaymentNotFoundException.class)
    public ResponseEntity<ApiErrorResponse> handlePaymentNotFound(PaymentNotFoundException e) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ApiErrorResponse.of(
                        HttpStatus.NOT_FOUND.value(),
                        "PAYMENT_NOT_FOUND",
                        e.getMessage()
                ));
    }

    // 포트원 API 호출 실패 (네트워크 오류, 응답 파싱 실패 등)
    @ExceptionHandler(PortOneApiException.class)
    public ResponseEntity<ApiErrorResponse> handlePortOneApiError(PortOneApiException e) {
        return ResponseEntity.status(HttpStatus.BAD_GATEWAY)
                .body(ApiErrorResponse.of(
                        HttpStatus.BAD_GATEWAY.value(),
                        "PORTONE_API_ERROR",
                        e.getMessage()
                ));
    }

    // 잘못된 플랜 값
    @ExceptionHandler(PaymentInvalidPlanException.class)
    public ResponseEntity<ApiErrorResponse> handleInvalidPlan(PaymentInvalidPlanException e) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiErrorResponse.of(
                        HttpStatus.BAD_REQUEST.value(),
                        "PAYMENT_INVALID_PLAN",
                        e.getMessage()
                ));
    }

    // PaymentExceptionHandler에 추가
    @ExceptionHandler(PaymentAlreadyVerifiedException.class)
    public ResponseEntity<ApiErrorResponse> handleAlreadyVerified(PaymentAlreadyVerifiedException e) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(ApiErrorResponse.of(
                        HttpStatus.CONFLICT.value(),
                        "PAYMENT_ALREADY_VERIFIED",
                        e.getMessage()
                ));
    }

    // 포트원에 결제 시도 기록이 없는 경우 (결제창을 아예 안 거친 상태로 verify 호출)
    @ExceptionHandler(PaymentNotAttemptedException.class)
    public ResponseEntity<ApiErrorResponse> handlePaymentNotAttempted(PaymentNotAttemptedException e) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ApiErrorResponse.of(
                        HttpStatus.NOT_FOUND.value(),
                        "PAYMENT_NOT_ATTEMPTED",
                        e.getMessage()
                ));
    }

    // 금액 불일치로 결제가 실패 처리된 경우
    @ExceptionHandler(PaymentAmountMismatchException.class)
    public ResponseEntity<ApiErrorResponse> handleAmountMismatch(PaymentAmountMismatchException e) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(ApiErrorResponse.of(
                        HttpStatus.CONFLICT.value(),
                        "PAYMENT_AMOUNT_MISMATCH",
                        e.getMessage()
                ));
    }

    // 취소 처리까지 실패해 수동 확인이 필요한 경우
    @ExceptionHandler(PaymentCancelFailedException.class)
    public ResponseEntity<ApiErrorResponse> handleCancelFailed(PaymentCancelFailedException e) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiErrorResponse.of(
                        HttpStatus.INTERNAL_SERVER_ERROR.value(),
                        "PAYMENT_CANCEL_FAILED",
                        e.getMessage()
                ));
    }

    // 결제 소유자가 아닌 경우
    @ExceptionHandler(PaymentAccessDeniedException.class)
    public ResponseEntity<ApiErrorResponse> handleAccessDenied(PaymentAccessDeniedException e) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(ApiErrorResponse.of(
                        HttpStatus.FORBIDDEN.value(),
                        "PAYMENT_ACCESS_DENIED",
                        e.getMessage()
                ));
    }
}
