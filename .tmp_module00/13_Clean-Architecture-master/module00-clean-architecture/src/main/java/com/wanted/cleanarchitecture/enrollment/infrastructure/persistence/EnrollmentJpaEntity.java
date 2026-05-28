package com.wanted.cleanarchitecture.enrollment.infrastructure.persistence;

import jakarta.persistence.*;

import java.time.Instant;

/*
 * EnrollmentJpaEntity는 enrollments 테이블을 다루기 위한 저장 전용 모델이다.
 * Aggregate의 상태 전이를 표현하는 Enrollment와 달리, 이 클래스는 JPA 매핑 책임만 가진다.
 */
@Entity
@Table(
        name = "enrollments",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_enrollments_user_course_status",
                columnNames = {"user_id", "course_id", "status"}
        )
)
public class EnrollmentJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "enrollment_id")
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "course_id", nullable = false)
    private Long courseId;

    @Column(name = "status", nullable = false)
    private String status;

    @Column(name = "enrolled_at", nullable = false)
    private Instant enrolledAt;

    @Column(name = "completed_at")
    private Instant completedAt;

    protected EnrollmentJpaEntity() {
    }

    public EnrollmentJpaEntity(Long userId, Long courseId, String status, Instant enrolledAt, Instant completedAt) {
        this.userId = userId;
        this.courseId = courseId;
        this.status = status;
        this.enrolledAt = enrolledAt;
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

    public Long getCourseId() {
        return courseId;
    }

    public String getStatus() {
        return status;
    }

    public Instant getEnrolledAt() {
        return enrolledAt;
    }

    public Instant getCompletedAt() {
        return completedAt;
    }
}
