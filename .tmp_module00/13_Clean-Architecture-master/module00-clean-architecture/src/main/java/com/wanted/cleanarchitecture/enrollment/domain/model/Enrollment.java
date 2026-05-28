package com.wanted.cleanarchitecture.enrollment.domain.model;

import com.wanted.cleanarchitecture.global.domain.common.exception.DomainRuleViolationException;

import java.time.Instant;

/*
 * [Entity / Aggregate 역할]
 * - 자기 상태만으로 판단 가능한 규칙/불변식을 책임진다.
 * - 이 클래스는 수강 상태 전이 규칙(중복 완료 방지)을 보장한다.
 */
public class Enrollment {

    private final Long id;
    private final Long userId;
    private final Long courseId;
    private EnrollmentStatus status;
    private final Instant enrolledAt;
    private Instant completedAt;

    private Enrollment(Long id, Long userId, Long courseId, EnrollmentStatus status, Instant enrolledAt, Instant completedAt) {
        if (userId == null || courseId == null) {
            throw new DomainRuleViolationException("Enrollment requires userId and courseId.");
        }
        this.id = id;
        this.userId = userId;
        this.courseId = courseId;
        this.status = status;
        this.enrolledAt = enrolledAt;
        this.completedAt = completedAt;
    }

    public static Enrollment create(Long userId, Long courseId) {
        return new Enrollment(null, userId, courseId, EnrollmentStatus.ACTIVE, Instant.now(), null);
    }

    public static Enrollment restore(Long id, Long userId, Long courseId, EnrollmentStatus status, Instant enrolledAt, Instant completedAt) {
        return new Enrollment(id, userId, courseId, status, enrolledAt, completedAt);
    }

    public void complete() {
        if (status == EnrollmentStatus.COMPLETED) {
            throw new DomainRuleViolationException("Enrollment already completed.");
        }
        this.status = EnrollmentStatus.COMPLETED;
        this.completedAt = Instant.now();
    }

    public Long getId() {
        return id;
    }

    public Long getUserId() {
        return userId;
    }

    public Long getCourseId() {
        return courseId;
    }

    public EnrollmentStatus getStatus() {
        return status;
    }

    public Instant getEnrolledAt() {
        return enrolledAt;
    }

    public Instant getCompletedAt() {
        return completedAt;
    }
}
