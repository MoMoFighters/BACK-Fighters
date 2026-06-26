package com.wanted.momocity.enrollment.application.service;

import com.wanted.momocity.enrollment.application.command.CreateEnrollmentCommand;
import com.wanted.momocity.enrollment.application.port.EnrollmentLecturePort;
import com.wanted.momocity.enrollment.application.port.StudentAccountPort;
import com.wanted.momocity.enrollment.application.usecase.EnrollmentCommandUseCase;
import com.wanted.momocity.enrollment.domain.event.EnrollmentCompletedEvent;
import com.wanted.momocity.enrollment.domain.exception.DuplicateEnrollmentException;
import com.wanted.momocity.enrollment.domain.exception.InvalidEnrollmentLectureStatusException;
import com.wanted.momocity.enrollment.domain.model.Building;
import com.wanted.momocity.enrollment.domain.model.Enrollment;
import com.wanted.momocity.enrollment.domain.repository.BuildingRepository;
import com.wanted.momocity.enrollment.domain.repository.EnrollmentRepository;
import com.wanted.momocity.global.domain.common.exception.DomainRuleViolationException;
import com.wanted.momocity.global.domain.model.Category;
import com.wanted.momocity.lecture.domain.model.LectureCategory;
import com.wanted.momocity.lecture.domain.model.LectureStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

// EnrollmentCommandService는 수강신청 생성 유스케이스를 처리하는 Application Service
@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class EnrollmentCommandService implements EnrollmentCommandUseCase {

    // 수강신청 저장소
    private final EnrollmentRepository enrollmentRepository;

    // 로그인 사용자 email을 studentId로 변환하기 위한 포트
    private final StudentAccountPort studentAccountPort;

    // 수강신청 대상 강의 상태를 조회하기 위한 포트
    private final EnrollmentLecturePort enrollmentLecturePort;

    // 수강신청 완료 이벤트를 발행하기 위한 Spring 이벤트 발행기
    private final ApplicationEventPublisher eventPublisher;

    // 수강신청 시 카테고리 건물 존재 여부 확인
    private final BuildingRepository buildingRepository;

    @Override
    public Enrollment createEnrollment(CreateEnrollmentCommand command) {

        log.info("수강신청 시작 - lectureId={}", command.lectureId());

            // Authorization 토큰에서 꺼낸 email로 학생 ID를 조회
            Long userId = studentAccountPort.getStudentId(command.studentId());

            // 수강신청 대상 강의의 현재 상태를 조회
            LectureStatus lectureStatus = enrollmentLecturePort.getLectureStatus(command.lectureId());

            // 수강신청할 강의의 카테고리 조회
            LectureCategory lectureCategory = enrollmentLecturePort.getLectureCategory(command.lectureId());

            // 같은 학생이 같은 강의를 이미 수강신청했는지 확인
            boolean alreadyEnrolled = enrollmentRepository.existsByUserIdAndLectureId(
                    userId,
                    command.lectureId()
            );

            // 이미 수강신청한 강의라면 중복 수강신청 예외를 발생
            if (alreadyEnrolled) {
                throw new DuplicateEnrollmentException("이미 수강신청한 강의입니다.");
            }

            // ACTIVE 상태의 강의만 수강신청할 수 있음
            if (lectureStatus != LectureStatus.ACTIVE) {
                throw new InvalidEnrollmentLectureStatusException("진행 중인 강의만 수강신청할 수 있습니다.");
            }

            // 해당 카테고리 건물이 없으면 새 건물 생성
            createBuildingIfAbsent(
                    userId,
                    lectureCategory,
                    command.position()
            );

            // 수강신청 도메인 객체를 생성
            Enrollment enrollment = Enrollment.create(
                    userId,
                    command.lectureId()
            );

            // 수강신청 정보를 저장
            Enrollment savedEnrollment = enrollmentRepository.save(enrollment);

            // 수강신청 완료 후
            // 1. 강사 자동 친구 추가를 위해
            // 2. 건물 누적 획득 카운트를 위해
            // 이벤트를 발행
            eventPublisher.publishEvent(new EnrollmentCompletedEvent(
                    savedEnrollment.getUserId(),
                    savedEnrollment.getLectureId()
            ));

            // 수강신청 완료와 이벤트 발행 여부를 추적하기 위한 로그
            // 수강신청 후 강사 자동 친구 추가 이벤트가 이어지므로 enrollmentId, userId, lectureId를 남긴다.
            log.info("수강신청 완료 - enrollmentId={}, userId={}, lectureId={}",
                    savedEnrollment.getId(),
                    savedEnrollment.getUserId(),
                    savedEnrollment.getLectureId());

            // 저장된 수강신청 도메인 객체를 반환
            return savedEnrollment;

    }

    private void createBuildingIfAbsent(
            Long userId,
            LectureCategory lectureCategory,
            Long position
    ) {
        Category buildingCategory = Category.valueOf(lectureCategory.name());
        // 사용자가 해당 카테고리 건물을 가지고 있는지 확인
        boolean hasBuilding = buildingRepository.existsByUserIdAndCategory(
                userId,
                buildingCategory
        );

        // 만약 건물을 이미 가지고 있으면 생성 X
        if (hasBuilding) {
            return;
        }

        // position 값 검증
        validateBuildingPosition(position);

        Building building = Building.create(
                userId,
                buildingCategory,
                position
        );

        buildingRepository.save(building);
    }

    private void validateBuildingPosition(Long position) {

        // 새 건물을 생성해야 되는데 position이 없으면
        if (position == null) {
            throw new DomainRuleViolationException("건물 값은 필수입니다.");
        }

        if (position < 1 || position > 5) {
            throw new DomainRuleViolationException("건물 위치 값은 1부터 5까지만 가능합니다.");
        }
    }
}