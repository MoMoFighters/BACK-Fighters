package com.wanted.cleanarchitecture.learning.application.usecase;

import java.time.Instant;

public interface LearningQueryUseCase {

    LearningProgressView handle(Long progressId);

    record LearningProgressView(
            Long progressId,
            Long userId,
            Long moduleId,
            String status,
            Instant completedAt
    ) {
    }
}
