package com.wanted.cleanarchitecture.catalog.application.usecase;

import com.wanted.cleanarchitecture.catalog.application.command.CreateCourseCommand;

public interface CourseCommandUseCase {

    // 실제 어플리케이션 비즈니스 로직 실행
    Long handle(CreateCourseCommand command);

}
