package com.wanted.cleanarchitecture.learning.infrastructure.adapter;

import com.wanted.cleanarchitecture.enrollment.domain.repository.EnrollmentRepository;
import com.wanted.cleanarchitecture.learning.application.port.EnrollmentAccessPort;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@Transactional(readOnly = true)
public class EnrollmentAccessAdapter implements EnrollmentAccessPort {

    private final EnrollmentRepository enrollmentRepository;

    public EnrollmentAccessAdapter(EnrollmentRepository enrollmentRepository) {
        this.enrollmentRepository = enrollmentRepository;
    }

    @Override
    public boolean hasActiveEnrollment(Long userId, Long courseId) {
        return enrollmentRepository.existsActiveEnrollment(userId, courseId);
    }
}
