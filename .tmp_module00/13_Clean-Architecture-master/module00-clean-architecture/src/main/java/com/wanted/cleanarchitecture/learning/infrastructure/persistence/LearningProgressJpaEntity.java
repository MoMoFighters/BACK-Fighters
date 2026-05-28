package com.wanted.cleanarchitecture.learning.infrastructure.persistence;

import jakarta.persistence.*;

import java.time.Instant;

/*
 * LearningProgressJpaEntity는 user_progress 테이블을 다루는 저장 모델이다.
 * domain의 LearningProgress와 분리해, 영속성 매핑과 비즈니스 상태 전이를 독립적으로 유지한다.
 */
@Entity
@Table(
        name = "user_progress",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_user_progress_user_module",
                columnNames = {"user_id", "module_id"}
        )
)
public class LearningProgressJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "progress_id")
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "module_id", nullable = false)
    private Long moduleId;

    @Column(name = "status", nullable = false)
    private String status;

    @Column(name = "completed_at")
    private Instant completedAt;

    protected LearningProgressJpaEntity() {
    }

    public LearningProgressJpaEntity(Long userId, Long moduleId, String status, Instant completedAt) {
        this.userId = userId;
        this.moduleId = moduleId;
        this.status = status;
        this.completedAt = completedAt;
    }

    public void changeStatus(String status) {
        this.status = status;
    }

    public void changeCompletedAt(Instant completedAt) {
        this.completedAt = completedAt;
    }

    public Long getId() {
        return id;
    }

    public Long getUserId() {
        return userId;
    }

    public Long getModuleId() {
        return moduleId;
    }

    public String getStatus() {
        return status;
    }

    public Instant getCompletedAt() {
        return completedAt;
    }
}
