package com.wanted.cleanarchitecture.catalog.domain.repository;

import com.wanted.cleanarchitecture.catalog.domain.model.Course;

import java.util.Optional;

/*
 * CourseRepository는 domain이 정의한 출력 포트다.
 * Aggregate를 어떤 저장 기술로 보관할지는 모르고, 필요한 연산 계약만 선언한다.
 * 학생들은 repository interface가 domain 쪽에 있어야 의존 방향이 안쪽을 향한다는 점을 기억하면 된다.
 */
public interface CourseRepository {

    Course save(Course course);

    Optional<Course> findById(Long courseId);
}
