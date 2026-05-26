package com.wanted.momocity.lecture.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

/*
 * JPA 전용 Repository다.
 * application/domain 계층에서는 직접 사용하지 않고,
 * LectureRepositoryAdapter 내부에서만 사용한다.
 */
public interface SpringDataLectureRepository extends JpaRepository<LectureJpaEntity, Long> {
}