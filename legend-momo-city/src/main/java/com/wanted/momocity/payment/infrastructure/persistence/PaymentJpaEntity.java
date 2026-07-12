package com.wanted.momocity.payment.infrastructure.persistence;

import com.wanted.momocity.payment.domain.model.Payment;
import com.wanted.momocity.payment.domain.model.Plan;
import com.wanted.momocity.payment.domain.model.Status;
import jakarta.persistence.*;
import lombok.Getter;

import java.time.LocalDateTime;

@Entity
@Table(name="payment")
@Getter
public class PaymentJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "payment_id", nullable = false, unique = true)
    private String paymentId; // 우리 서버가 생성해서 포트원 결제 요청/조회에 사용하는 고유 ID

    @Enumerated(EnumType.STRING)
    @Column(name = "plan", nullable = false)
    private Plan plan;

    @Column(name = "price", nullable = false)
    private Long price; // 결제 시점 금액

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private Status status;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    // id 포함 생성자
    public PaymentJpaEntity(Long id, Long userId, String paymentId, Plan plan, Long price,
                            Status status, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.userId = userId;
        this.paymentId = paymentId;
        this.plan = plan;
        this.price = price;
        this.status = status;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    protected PaymentJpaEntity() {}

    // 업데이트 될 때 현재 시각으로 자동 세팅
    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    public static PaymentJpaEntity fromDomain(Payment payment) {
        return new PaymentJpaEntity(
                payment.getId(),
                payment.getUserId(),
                payment.getPaymentId(),
                payment.getPlan(),
                payment.getPrice(),
                payment.getStatus(),
                payment.getCreatedAt(),
                payment.getUpdatedAt()
        );
    }

    public Payment toDomain() {
        return new Payment(id, userId, paymentId, plan, price, status, createdAt, updatedAt);
    }
}
