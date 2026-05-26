package com.wanted.momocity.enrollment.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface EnrollmentJpaRepository extends JpaRepository<EnrollmentJpaEntity, Long> {

    Optional<EnrollmentJpaEntity> findByUserIdAndLectureId(Long userId, Long lectureId);

    List<EnrollmentJpaEntity> findAllByUserId(Long userId);
}
