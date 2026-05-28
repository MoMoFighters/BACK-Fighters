package com.wanted.cleanarchitecture.enrollment.application.service;

import com.wanted.cleanarchitecture.enrollment.application.usecase.EnrollmentQueryUseCase;
import com.wanted.cleanarchitecture.enrollment.domain.model.Enrollment;
import com.wanted.cleanarchitecture.enrollment.domain.repository.EnrollmentRepository;
import com.wanted.cleanarchitecture.global.domain.common.exception.DomainRuleViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class EnrollmentQueryService implements EnrollmentQueryUseCase {

    private final EnrollmentRepository enrollmentRepository;

    public EnrollmentQueryService(EnrollmentRepository enrollmentRepository) {
        this.enrollmentRepository = enrollmentRepository;
    }

    @Override
    public EnrollmentView handle(Long enrollmentId) {
        Enrollment enrollment = enrollmentRepository.findById(enrollmentId)
                .orElseThrow(() -> new DomainRuleViolationException("Enrollment not found: " + enrollmentId));
        return new EnrollmentView(
                enrollment.getId(),
                enrollment.getUserId(),
                enrollment.getCourseId(),
                enrollment.getStatus().name(),
                enrollment.getEnrolledAt(),
                enrollment.getCompletedAt()
        );
    }
}
