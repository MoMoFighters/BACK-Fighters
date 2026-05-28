package com.wanted.cleanarchitecture.catalog.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

/*
 * SpringDataCourseRepository는 Spring Data JPA가 제공하는 기술 구현체다.
 * 이 인터페이스는 infrastructure 내부에서만 사용하고, application/domain은 알지 못한다.
 */
public interface SpringDataCourseRepository extends JpaRepository<CourseJpaEntity, Long> {
}
