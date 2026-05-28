package com.wanted.cleanarchitecture.enrollment.infrastructure.persistence;

import com.wanted.cleanarchitecture.enrollment.domain.model.Enrollment;
import com.wanted.cleanarchitecture.enrollment.domain.model.EnrollmentStatus;
import com.wanted.cleanarchitecture.enrollment.domain.repository.EnrollmentRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

/*
 * EnrollmentRepositoryAdapter는 EnrollmentRepository port의 JPA 구현체다.
 * 이 클래스의 핵심 책임은 domain model과 JPA entity 사이의 변환이며, 비즈니스 규칙을 판단하지는 않는다.
 */
@Repository
@Transactional
public class EnrollmentRepositoryAdapter implements EnrollmentRepository {

    private final SpringDataEnrollmentRepository repository;

    public EnrollmentRepositoryAdapter(SpringDataEnrollmentRepository repository) {
        this.repository = repository;
    }

    @Override
    public Enrollment save(Enrollment enrollment) {
        // 새 Aggregate와 기존 Aggregate 저장 흐름을 통일해 application에는 같은 port만 노출한다.
        EnrollmentJpaEntity entity = enrollment.getId() == null
                ? new EnrollmentJpaEntity(
                        enrollment.getUserId(),
                        enrollment.getCourseId(),
                        enrollment.getStatus().name().toLowerCase(),
                        enrollment.getEnrolledAt(),
                        enrollment.getCompletedAt()
                )
                : repository.findById(enrollment.getId()).orElseThrow();

        entity.changeStatus(enrollment.getStatus().name().toLowerCase());
        entity.changeCompletedAt(enrollment.getCompletedAt());

        EnrollmentJpaEntity saved = repository.save(entity);
        return toDomain(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Enrollment> findById(Long enrollmentId) {
        return repository.findById(enrollmentId).map(this::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsActiveEnrollment(Long userId, Long courseId) {
        return repository.existsByUserIdAndCourseIdAndStatus(userId, courseId, EnrollmentStatus.ACTIVE.name().toLowerCase());
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Enrollment> findActiveEnrollment(Long userId, Long courseId) {
        return repository.findByUserIdAndCourseIdAndStatus(userId, courseId, EnrollmentStatus.ACTIVE.name().toLowerCase())
                .map(this::toDomain);
    }

    private Enrollment toDomain(EnrollmentJpaEntity entity) {
        // 저장 모델을 순수 domain model로 바꿔야 상위 계층이 JPA에 오염되지 않는다.
        return Enrollment.restore(
                entity.getId(),
                entity.getUserId(),
                entity.getCourseId(),
                EnrollmentStatus.valueOf(entity.getStatus().toUpperCase()),
                entity.getEnrolledAt(),
                entity.getCompletedAt()
        );
    }
}
