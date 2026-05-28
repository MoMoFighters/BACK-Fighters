package com.wanted.cleanarchitecture.catalog.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

/* comment.
*   SpringDataCourseRepository 해당 클래스는 Spring Data JPA 의존성이 제공하는
*   기술 구현체이다.
*   해당 인터페이스는 infrastructure 내부에서만 사용하며,
*   domain / application 패키지는 알지 못한다.
*  */
public interface SpringDataCourseRepository extends JpaRepository<CourseJpaEntity , Long> {
}
