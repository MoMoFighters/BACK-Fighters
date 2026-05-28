package com.wanted.cleanarchitecture.enrollment.application.usecase;

import com.wanted.cleanarchitecture.enrollment.application.command.EnrollCourseCommand;

public interface EnrollmentCommandUseCase {

    Long handle(EnrollCourseCommand command);
}
