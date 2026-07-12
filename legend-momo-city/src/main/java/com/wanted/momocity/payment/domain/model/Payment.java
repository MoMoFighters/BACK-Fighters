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
}
