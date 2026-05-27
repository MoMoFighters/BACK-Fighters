package com.wanted.momocity.enrollment.application.usecase;

import com.wanted.momocity.enrollment.application.command.CreateEnrollmentCommand;
import com.wanted.momocity.enrollment.domain.model.Enrollment;

// EnrollmentCommandUseCase는 수강신청 쓰기 기능 담당
public interface EnrollmentCommandUseCase {

    // 수강신청을 생성합니다.
    Enrollment createEnrollment(CreateEnrollmentCommand command);
}