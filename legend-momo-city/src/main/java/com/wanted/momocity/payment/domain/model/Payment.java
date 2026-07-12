package com.wanted.momocity.payment.domain.model;

import java.time.LocalDateTime;

public class Payment {

    private final Long id;
    private final Long userId;
    private final Long paymentId;
    private final Plan plan;
    private final Long price;
    private final Status status;
    private final LocalDateTime createdAt;
    private final LocalDateTime updatedAt;


    public Payment(Long id, Long userId, Long paymentId, Plan plan, Long price, Status status, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.userId = userId;
        this.paymentId = paymentId;
        this.plan = plan;
        this.price = price;
        this.status = status;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }
}
