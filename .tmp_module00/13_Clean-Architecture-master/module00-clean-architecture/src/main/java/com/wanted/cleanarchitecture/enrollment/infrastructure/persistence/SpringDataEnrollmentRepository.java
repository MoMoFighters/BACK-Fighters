package com.wanted.cleanarchitecture.enrollment.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/*
 * SpringDataEnrollmentRepository는 infrastructure 내부에서만 사용하는 기술 저장소다.
 * domain port를 직접 구현하지 않고, adapter가 이 기술 저장소를 감싸는 구조를 의도적으로 보여준다.
 */
public interface SpringDataEnrollmentRepository extends JpaRepository<EnrollmentJpaEntity, Long> {

    boolean existsByUserIdAndCourseIdAndStatus(Long userId, Long courseId, String status);

    Optional<EnrollmentJpaEntity> findByUserIdAndCourseIdAndStatus(Long userId, Long courseId, String status);
}
