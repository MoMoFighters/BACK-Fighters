package com.wanted.cleanarchitecture.learning.application.service;

import com.wanted.cleanarchitecture.global.domain.common.exception.DomainRuleViolationException;
import com.wanted.cleanarchitecture.learning.application.usecase.LearningQueryUseCase;
import com.wanted.cleanarchitecture.learning.domain.model.LearningProgress;
import com.wanted.cleanarchitecture.learning.domain.repository.LearningProgressRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class LearningQueryService implements LearningQueryUseCase {

    private final LearningProgressRepository learningProgressRepository;

    public LearningQueryService(LearningProgressRepository learningProgressRepository) {
        this.learningProgressRepository = learningProgressRepository;
    }

    @Override
    public LearningProgressView handle(Long progressId) {
        LearningProgress progress = learningProgressRepository.findById(progressId)
                .orElseThrow(() -> new DomainRuleViolationException("Learning progress not found: " + progressId));
        return new LearningProgressView(
                progress.getId(),
                progress.getUserId(),
                progress.getModuleId(),
                progress.getStatus().name(),
                progress.getCompletedAt()
        );
    }
}
