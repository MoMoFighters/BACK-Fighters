package com.wanted.cleanarchitecture.enrollment.application.usecase;

import java.time.Instant;

public interface EnrollmentQueryUseCase {

    EnrollmentView handle(Long enrollmentId);

    record EnrollmentView(
            Long enrollmentId,
            Long userId,
            Long courseId,
            String status,
            Instant enrolledAt,
            Instant completedAt
    ) {
    }
}
