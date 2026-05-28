package com.wanted.cleanarchitecture.learning.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/*
 * SpringDataLearningProgressRepository는 infrastructure 내부 기술 저장소다.
 * application/domain은 이 타입을 직접 의존하지 않고 adapter 뒤에 숨긴다.
 */
public interface SpringDataLearningProgressRepository extends JpaRepository<LearningProgressJpaEntity, Long> {

    Optional<LearningProgressJpaEntity> findByUserIdAndModuleId(Long userId, Long moduleId);
}
