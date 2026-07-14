package com.wanted.momocity.payment.domain.model;

public enum Status {
    // 결제 상태
    PENDING, SUCCESS, FAILED, REFUND, CANCEL_FAILED  // 결제는 완료됐지만(금액 불일치 등으로) 취소 처리가 실패한 상태

}
