package com.wanted.momocity.order.infrastructure.persistence;

import com.wanted.momocity.order.domain.model.ListResult;
import com.wanted.momocity.order.domain.model.Reason;
import com.wanted.momocity.order.domain.model.Type;
import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "order_history", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"user_id", "item_id"})
})
public class OrderJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long userId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Reason reason;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Type type;

    @Column(nullable = false)
    private Long amount;

    @Column(nullable = true) // 포인트 + 이면 itemId가 없음
    private Long itemId; // store테이블에 있는 상품의 pk

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public OrderJpaEntity() {
    }

    public OrderJpaEntity(Long userId, Reason reason, Type type, Long amount, Long itemId, LocalDateTime createdAt) {
        this.userId = userId;
        this.reason = reason;
        this.type = type;
        this.amount = amount;
        this.itemId = itemId;
        this.createdAt = createdAt;
    }

    public Long getId() {
        return id;
    }

    public Long getUserId() {
        return userId;
    }

    public Reason getReason() {
        return reason;
    }

    public Type getType() {
        return type;
    }

    public Long getAmount() {
        return amount;
    }

    public Long getItemId() {
        return itemId;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public ListResult toDomain() {
        return new ListResult(type, reason, createdAt, amount);
    }

}
