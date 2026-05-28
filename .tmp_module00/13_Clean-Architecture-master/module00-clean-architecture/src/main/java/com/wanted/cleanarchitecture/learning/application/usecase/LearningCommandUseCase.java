package com.wanted.cleanarchitecture.learning.application.usecase;

import com.wanted.cleanarchitecture.learning.application.command.CompleteModuleCommand;

public interface LearningCommandUseCase {

    void handle(CompleteModuleCommand command);
}
