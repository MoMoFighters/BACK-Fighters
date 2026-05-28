package com.wanted.cleanarchitecture.catalog.domain.repository;

import com.wanted.cleanarchitecture.catalog.domain.model.Course;

import java.util.Optional;

public interface CourseRepository {

    Course save(Course newCourse);
    // 강의 상세 조회
    Optional<Course> findById(Long courseId);
}
