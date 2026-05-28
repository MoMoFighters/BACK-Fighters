package com.wanted.cleanarchitecture.learning.domain.model;

import com.wanted.cleanarchitecture.global.domain.common.exception.DomainRuleViolationException;

import java.time.Instant;

/*
 * [Entity / Aggregate 역할]
 * - 자기 상태만으로 판단 가능한 규칙/불변식을 책임진다.
 * - 이 클래스는 모듈 완료 상태 전이와 중복 완료 방지 규칙을 보장한다.
 */
public class LearningProgress {

    private final Long id;
    private final Long userId;
    private final Long moduleId;
    private ProgressStatus status;
    private Instant completedAt;

    private LearningProgress(Long id, Long userId, Long moduleId, ProgressStatus status, Instant completedAt) {
        this.id = id;
        this.userId = userId;
        this.moduleId = moduleId;
        this.status = status;
        this.completedAt = completedAt;
    }

    public static LearningProgress start(Long userId, Long moduleId) {
        return new LearningProgress(null, userId, moduleId, ProgressStatus.STARTED, null);
    }

    public static LearningProgress restore(Long id, Long userId, Long moduleId, ProgressStatus status, Instant completedAt) {
        return new LearningProgress(id, userId, moduleId, status, completedAt);
    }

    public void complete() {
        if (status == ProgressStatus.COMPLETED) {
            throw new DomainRuleViolationException("Module already completed.");
        }
        this.status = ProgressStatus.COMPLETED;
        this.completedAt = Instant.now();
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

    public ProgressStatus getStatus() {
        return status;
    }

    public Instant getCompletedAt() {
        return completedAt;
    }
}
