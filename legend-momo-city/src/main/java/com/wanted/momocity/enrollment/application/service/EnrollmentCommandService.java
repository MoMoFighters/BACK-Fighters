package com.wanted.momocity.enrollment.application.service;

import com.wanted.momocity.enrollment.application.command.CreateEnrollmentCommand;
import com.wanted.momocity.enrollment.application.port.EnrollmentLecturePort;
import com.wanted.momocity.enrollment.application.port.StudentAccountPort;
import com.wanted.momocity.enrollment.application.usecase.EnrollmentCommandUseCase;
import com.wanted.momocity.enrollment.domain.exception.DuplicateEnrollmentException;
import com.wanted.momocity.enrollment.domain.exception.InvalidEnrollmentLectureStatusException;
import com.wanted.momocity.enrollment.domain.model.Enrollment;
import com.wanted.momocity.enrollment.domain.repository.EnrollmentRepository;
import com.wanted.momocity.global.domain.common.exception.DomainRuleViolationException;
import com.wanted.momocity.lecture.domain.model.LectureStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

// EnrollmentCommandService는 수강신청 생성 로직을 처리하는 서비스입니다.
@Service
@RequiredArgsConstructor
@Transactional
public class EnrollmentCommandService implements EnrollmentCommandUseCase {

    // 수강신청
    private final EnrollmentRepository enrollmentRepository;
    private final StudentAccountPort studentAccountPort;
    private final EnrollmentLecturePort enrollmentLecturePort;
    /*
     * 수강신청을 생성
     * 처리 순서:
     *  1. 이미 수강신청한 강의인지 확인
     *  2. 중복이면 예외 발생
     *  3. Enrollment 도메인 객체 생성
     *  4. 저장소에 저장
     *  5. 저장된 Enrollment 반환
     */
    @Override
    public Enrollment createEnrollment(CreateEnrollmentCommand command) {

        Long userId = studentAccountPort.getStudentId(command.studentEmail());
        LectureStatus lectureStatus = enrollmentLecturePort.getLectureStatus(command.lectureId());

        // 같은 사용자가 같은 강의를 이미 신청했는지 확인합니다.
        boolean alreadyEnrolled = enrollmentRepository.existsByUserIdAndLectureId(
                userId,
                command.lectureId()
        );

        // 이미 신청했다면 409 Error 발생
        if (alreadyEnrolled) {
            throw new DuplicateEnrollmentException("이미 수강신청한 강의입니다.");
        }

        if (lectureStatus != LectureStatus.ACTIVE) {
            throw new InvalidEnrollmentLectureStatusException("진행 중인 강의만 수강신청할 수 있습니다.");
        }

        // 새 수강신청 도메인 객체 생성
        Enrollment enrollment = Enrollment.create(
                userId,
                command.lectureId()
        );

        // 수강신청 정보를 저장하고, 저장된 도메인 객체를 반환
        return enrollmentRepository.save(enrollment);
    }
}