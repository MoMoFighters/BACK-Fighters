package com.wanted.cleanarchitecture.catalog.application.service;

import com.wanted.cleanarchitecture.catalog.application.usecase.CourseQueryUseCase;
import com.wanted.cleanarchitecture.catalog.domain.model.Course;
import com.wanted.cleanarchitecture.catalog.domain.repository.CourseRepository;
import com.wanted.cleanarchitecture.global.domain.common.exception.DomainRuleViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class CourseQueryService implements CourseQueryUseCase {

    private final CourseRepository courseRepository;

    public CourseQueryService(CourseRepository courseRepository) {
        this.courseRepository = courseRepository;
    }

    @Override
    public CourseView handle(Long courseId) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new DomainRuleViolationException("Course not found: " + courseId));

        int moduleCount = course.getSections().stream()
                .mapToInt(section -> section.getModules().size())
                .sum();
        return new CourseView(
                course.getId(),
                course.getAuthorId(),
                course.getTitle(),
                course.getDescription(),
                course.getStatus().name(),
                course.getSections().size(),
                moduleCount
        );
    }
}
