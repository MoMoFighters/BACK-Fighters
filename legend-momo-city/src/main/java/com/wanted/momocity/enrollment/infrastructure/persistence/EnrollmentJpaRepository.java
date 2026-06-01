package com.wanted.momocity.enrollment.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

/*
 * EnrollmentJpaRepository는 enrollment 테이블에 접근하는 JPA Repository입니다.
 * 이 인터페이스는 Spring Data JPA가 자동으로 구현체를 만들어준다.
 */
public interface EnrollmentJpaRepository extends JpaRepository<EnrollmentJpaEntity, Long> {

    /*
     * 특정 사용자가 특정 강의를 이미 수강신청했는지 확인
     * 중복 수강신청 방지에 사용
     * 이미 신청한 기록이 있으면 true,
     * 없으면 false를 반환
     */
    boolean existsByUserIdAndLectureId(Long userId, Long lectureId);

    // 특정 사용자의 특정 강의 수강신청 정보를 조회합니다.
    Optional<EnrollmentJpaEntity> findByUserIdAndLectureId(Long userId, Long lectureId);

    /*
     * 특정 사용자의 모든 수강신청 목록을 조회합니다.
     * 나중에 GET /api/enrollments
     * 즉, 내 수강 내역 조회에서 사용합니다.
     */
    List<EnrollmentJpaEntity> findAllByUserId(Long userId);
}