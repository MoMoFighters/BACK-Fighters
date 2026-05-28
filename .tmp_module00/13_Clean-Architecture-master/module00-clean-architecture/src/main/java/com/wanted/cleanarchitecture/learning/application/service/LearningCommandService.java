package com.wanted.cleanarchitecture.learning.application.service;

import com.wanted.cleanarchitecture.learning.application.command.CompleteModuleCommand;
import com.wanted.cleanarchitecture.learning.application.policy.LearningAccessPolicy;
import com.wanted.cleanarchitecture.learning.application.usecase.LearningCommandUseCase;
import com.wanted.cleanarchitecture.learning.domain.model.LearningProgress;
import com.wanted.cleanarchitecture.learning.domain.repository.LearningProgressRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/*
 * [Application Service 역할]
 * - 트랜잭션 경계 안에서 Policy 검증 + Entity 상태 전이 + 저장을 오케스트레이션한다.
 * - 규칙 구현은 Entity/Policy에 두고, Service는 실행 순서와 경계에 집중한다.
 */
@Service
@Transactional
public class LearningCommandService implements LearningCommandUseCase {

    private final LearningProgressRepository learningProgressRepository;
    private final LearningAccessPolicy learningAccessPolicy;

    public LearningCommandService(
            LearningProgressRepository learningProgressRepository,
            LearningAccessPolicy learningAccessPolicy
    ) {
        this.learningProgressRepository = learningProgressRepository;
        this.learningAccessPolicy = learningAccessPolicy;
    }

    @Override
    public void handle(CompleteModuleCommand command) {
        learningAccessPolicy.ensureModuleCompletable(command.userId(), command.courseId());

        LearningProgress progress = learningProgressRepository
                .findByUserIdAndModuleId(command.userId(), command.moduleId())
                .orElseGet(() -> LearningProgress.start(command.userId(), command.moduleId()));

        progress.complete();
        learningProgressRepository.save(progress);
    }
}
