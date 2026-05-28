package com.wanted.cleanarchitecture.learning.domain.repository;

import com.wanted.cleanarchitecture.learning.domain.model.LearningProgress;

import java.util.Optional;

/*
 * LearningProgressRepository는 learning context의 출력 포트다.
 * 진행 기록 저장 방식은 숨기고, 유스케이스가 필요한 저장/조회 계약만 노출한다.
 */
public interface LearningProgressRepository {

    LearningProgress save(LearningProgress progress);

    Optional<LearningProgress> findById(Long progressId);

    Optional<LearningProgress> findByUserIdAndModuleId(Long userId, Long moduleId);
}
