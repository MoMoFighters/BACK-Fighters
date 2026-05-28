package com.wanted.cleanarchitecture.learning.infrastructure.persistence;

import com.wanted.cleanarchitecture.learning.domain.model.LearningProgress;
import com.wanted.cleanarchitecture.learning.domain.model.ProgressStatus;
import com.wanted.cleanarchitecture.learning.domain.repository.LearningProgressRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

/*
 * LearningProgressRepositoryAdapter는 learning context의 repository port 구현체다.
 * 저장소 세부 구현을 바깥 계층에 묶어두고, 안쪽 계층에는 순수 domain model만 넘긴다.
 */
@Repository
@Transactional
public class LearningProgressRepositoryAdapter implements LearningProgressRepository {

    private final SpringDataLearningProgressRepository repository;

    public LearningProgressRepositoryAdapter(SpringDataLearningProgressRepository repository) {
        this.repository = repository;
    }

    @Override
    public LearningProgress save(LearningProgress progress) {
        LearningProgressJpaEntity entity = progress.getId() == null
                ? new LearningProgressJpaEntity(
                        progress.getUserId(),
                        progress.getModuleId(),
                        progress.getStatus().name().toLowerCase(),
                        progress.getCompletedAt()
                )
                : repository.findById(progress.getId()).orElseThrow();

        entity.changeStatus(progress.getStatus().name().toLowerCase());
        entity.changeCompletedAt(progress.getCompletedAt());

        LearningProgressJpaEntity saved = repository.save(entity);
        return toDomain(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<LearningProgress> findById(Long progressId) {
        return repository.findById(progressId).map(this::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<LearningProgress> findByUserIdAndModuleId(Long userId, Long moduleId) {
        return repository.findByUserIdAndModuleId(userId, moduleId).map(this::toDomain);
    }

    private LearningProgress toDomain(LearningProgressJpaEntity entity) {
        return LearningProgress.restore(
                entity.getId(),
                entity.getUserId(),
                entity.getModuleId(),
                ProgressStatus.valueOf(entity.getStatus().toUpperCase()),
                entity.getCompletedAt()
        );
    }
}
