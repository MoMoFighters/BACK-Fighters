package com.wanted.momocity.payment.domain.model;

public enum Status {
    // 결제 상태
    PENDING, SUCCESS, FAILED, REFUND, CANCEL_FAILED  // 결제는 완료됐지만(금액 불일치 등으로) 취소 처리가 실패한 상태
    /*comment
    *  Pending : 결제 준비 상태
    *  Success : 결제
    *  Refund : 환불
    *  Failed : 결제 실패
    *  Cancel_Failed : 결제 취소 실패 */

}
