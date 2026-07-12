package com.wanted.momocity.payment.domain.model;

import java.time.LocalDateTime;

public class Payment {

    private final Long id;
    private final Long userId;
    private final String paymentId;
    private final Plan plan;
    private final Long price;
    private final Status status;
    private final LocalDateTime createdAt;
    private final LocalDateTime updatedAt;


    public Payment(Long id, Long userId, String paymentId, Plan plan, Long price, Status status, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.userId = userId;
        this.paymentId = paymentId;
        this.plan = plan;
        this.price = price;
        this.status = status;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    // prepare 단계
    public static Payment createPending(Long userId, String paymentId, Plan plan, Long price) {
        LocalDateTime now = LocalDateTime.now();
        return new Payment(null, userId, paymentId, plan, price, Status.PENDING, now, now);
    }

    // getter
    public Long getId() { return id; }
    public Long getUserId() { return userId; }
    public String getPaymentId() { return paymentId; }
    public Plan getPlan() { return plan; }
    public Long getPrice() { return price; }
    public Status getStatus() { return status; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }

     // 포트원 실결제금액과 DB 금액이 일치할
    public Payment markSuccess() {
        return new Payment(this.id, this.userId, this.paymentId, this.plan, this.price,
                Status.SUCCESS, this.createdAt, LocalDateTime.now());
    }


    // 금액 불일치 또는 결제 실패 시 호출 (pgTxId는 남겨서 어떤 포트원 거래건이었는지 추적 가능하게)
    public Payment markFailed() {
        return new Payment(this.id, this.userId, this.paymentId, this.plan, this.price,
                Status.FAILED, this.createdAt, LocalDateTime.now());
    }

    // 금액 불일치로 취소를 시도했으나 포트원 취소 API 자체가 실패한 경우
    // 실제 돈은 포트원 쪽에 잡혀 있을 수 있어 수동 확인이 필요한 상태
    public Payment markCancelFailed() {
        return new Payment(this.id, this.userId, this.paymentId, this.plan, this.price,
                Status.CANCEL_FAILED, this.createdAt, LocalDateTime.now());
    }

    public boolean isFinalized() {
        return this.status == Status.SUCCESS
                || this.status == Status.FAILED
                || this.status == Status.CANCEL_FAILED;
    }
}
