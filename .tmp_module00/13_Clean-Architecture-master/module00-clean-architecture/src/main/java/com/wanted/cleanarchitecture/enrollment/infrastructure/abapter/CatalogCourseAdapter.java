package com.wanted.cleanarchitecture.enrollment.infrastructure.abapter;

import com.wanted.cleanarchitecture.catalog.domain.model.CourseStatus;
import com.wanted.cleanarchitecture.catalog.domain.repository.CourseRepository;
import com.wanted.cleanarchitecture.enrollment.application.port.CourseCatalogPort;
import com.wanted.cleanarchitecture.enrollment.application.port.CoursePublicationStatus;
import com.wanted.cleanarchitecture.global.domain.common.exception.DomainRuleViolationException;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/*
 * CatalogCourseAdapter는 enrollment가 정의한 CourseCatalogPort를 실제 catalog 조회로 구현한다.
 *
 * 이 클래스가 Infrastructure에 있는 이유:
 * - 다른 Bounded Context에 접근하는 방법은 기술적 연결 세부사항이다.
 * - 지금은 같은 애플리케이션 안의 CourseRepository를 사용하지만,
 *   나중에는 HTTP Client, 메시지 조회, 별도 read model 조회로 바뀔 수 있다.
 * - 그런 변화가 EnrollmentEligibilityPolicy에 퍼지지 않도록 Adapter에 숨긴다.
 *
 * 의존 방향:
 * - enrollment.application은 CourseCatalogPort만 안다.
 * - enrollment.infrastructure는 CourseCatalogPort를 구현하기 위해 catalog의 Repository를 안다.
 *
 * 수업용 정리:
 * Policy는 "무엇이 필요한지"를 Port로 말하고,
 * Adapter는 "그것을 어떻게 가져올지"를 구현한다.
 */
@Component
@Transactional(readOnly = true)
public class CatalogCourseAdapter implements CourseCatalogPort {

    private final CourseRepository courseRepository;

    public CatalogCourseAdapter(CourseRepository courseRepository) {
        this.courseRepository = courseRepository;
    }

    @Override
    public CoursePublicationStatus getPublicationStatus(Long courseId) {
        return courseRepository.findById(courseId)
                .map(course -> new CoursePublicationStatus(
                        course.getId(),
                        course.getStatus() == CourseStatus.PUBLISHED
                ))
                .orElseThrow(() -> new DomainRuleViolationException("Course not found: " + courseId));
    }
}
