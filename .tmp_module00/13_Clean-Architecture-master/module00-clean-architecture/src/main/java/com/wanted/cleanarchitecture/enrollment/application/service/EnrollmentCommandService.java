package com.wanted.cleanarchitecture.enrollment.application.service;

import com.wanted.cleanarchitecture.enrollment.application.command.EnrollCourseCommand;
import com.wanted.cleanarchitecture.enrollment.application.policy.EnrollmentEligibilityPolicy;
import com.wanted.cleanarchitecture.enrollment.application.usecase.EnrollmentCommandUseCase;
import com.wanted.cleanarchitecture.enrollment.domain.model.Enrollment;
import com.wanted.cleanarchitecture.enrollment.domain.repository.EnrollmentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/*
 * EnrollmentCommandService는 수강 신청 UseCase를 조립하는 Application Service다.
 *
 * 계층별 호출 흐름:
 * EnrollmentController
 *   -> EnrollCourseCommand
 *      -> EnrollmentCommandService
 *         -> EnrollmentEligibilityPolicy
 *            -> CourseCatalogPort
 *         -> Enrollment.create()
 *         -> EnrollmentRepository.save()
 *
 * 이 Service가 catalog의 Course를 직접 조회하지 않는 이유:
 * - enrollment 컨텍스트는 Course 전체 구조를 알 필요가 없다.
 * - 수강 신청에 필요한 것은 "강의가 공개되었는가?"라는 사실뿐이다.
 * - 그 사실은 EnrollmentEligibilityPolicy가 CourseCatalogPort를 통해 확인한다.
 */
@Service
@Transactional
public class EnrollmentCommandService implements EnrollmentCommandUseCase {

    private final EnrollmentEligibilityPolicy enrollmentEligibilityPolicy;
    private final EnrollmentRepository enrollmentRepository;

    public EnrollmentCommandService(
            EnrollmentEligibilityPolicy enrollmentEligibilityPolicy,
            EnrollmentRepository enrollmentRepository
    ) {
        this.enrollmentEligibilityPolicy = enrollmentEligibilityPolicy;
        this.enrollmentRepository = enrollmentRepository;
    }

    @Override
    public Long handle(EnrollCourseCommand command) {
        enrollmentEligibilityPolicy.ensureEligible(command.userId(), command.courseId());

        Enrollment saved = enrollmentRepository.save(Enrollment.create(command.userId(), command.courseId()));
        return saved.getId();
    }
}
