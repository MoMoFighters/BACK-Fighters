package com.wanted.momocity.enrollment.domain.repository;

import com.wanted.momocity.enrollment.domain.model.Enrollment;

import java.util.List;
import java.util.Optional;

// EnrollmentRepository는 수강신청 저장소의 규칙을 정하는 인터페이스
public interface EnrollmentRepository {

    /*
     * 수강신청 정보를 저장
     * 새로 수강신청할 때 사용
     */
    Enrollment save(Enrollment enrollment);

    /*
     * 특정 사용자가 특정 강의를 이미 수강신청했는지 확인
     * 중복 수강신청 방지에 사용
     */
    boolean existsByUserIdAndLectureId(Long userId, Long lectureId);

    /*
     * 특정 사용자의 특정 강의 수강신청 정보를 조회
     * viewing 쪽에서 "이 사용자가 이 강의를 볼 수 있는가?" 확인
     */
    Optional<Enrollment> findByUserIdAndLectureId(Long userId, Long lectureId);

    // 특정 사용자의 전체 수강신청 목록을 조회
    List<Enrollment> findAllByUserId(Long userId);
}