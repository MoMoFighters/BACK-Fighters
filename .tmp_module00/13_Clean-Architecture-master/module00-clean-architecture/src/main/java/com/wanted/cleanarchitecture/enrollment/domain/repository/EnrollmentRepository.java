package com.wanted.cleanarchitecture.enrollment.domain.repository;

import com.wanted.cleanarchitecture.enrollment.domain.model.Enrollment;

import java.util.Optional;

/*
 * EnrollmentRepository는 enrollment context의 repository port다.
 * application/domain은 "활성 수강 존재 여부" 같은 비즈니스에 필요한 조회만 의존하고,
 * 실제 SQL이나 JPA 방식은 adapter로 밀어낸다.
 */
public interface EnrollmentRepository {

    Enrollment save(Enrollment enrollment);

    Optional<Enrollment> findById(Long enrollmentId);

    boolean existsActiveEnrollment(Long userId, Long courseId);

    Optional<Enrollment> findActiveEnrollment(Long userId, Long courseId);
}
