package com.wanted.cleanarchitecture.catalog.application.usecase;

public interface CourseQueryUseCase {

    CourseView handle(Long courseId);

    record CourseView(
            Long courseId,
            Long authorId,
            String title,
            String description,
            String status,
            int sectionCount,
            int moduleCount
    ) {
    }
}
