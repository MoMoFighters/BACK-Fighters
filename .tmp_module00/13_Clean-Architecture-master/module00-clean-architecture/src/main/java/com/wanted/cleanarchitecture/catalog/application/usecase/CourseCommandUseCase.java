package com.wanted.cleanarchitecture.catalog.application.usecase;

import com.wanted.cleanarchitecture.catalog.application.command.AddModuleCommand;
import com.wanted.cleanarchitecture.catalog.application.command.AddSectionCommand;
import com.wanted.cleanarchitecture.catalog.application.command.CreateCourseCommand;
import com.wanted.cleanarchitecture.catalog.application.command.PublishCourseCommand;

public interface CourseCommandUseCase {

    Long handle(CreateCourseCommand command);

    void handle(AddSectionCommand command);

    void handle(AddModuleCommand command);

    void handle(PublishCourseCommand command);
}
