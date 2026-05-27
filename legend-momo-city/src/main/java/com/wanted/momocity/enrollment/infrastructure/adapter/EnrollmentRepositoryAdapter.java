package com.wanted.momocity.enrollment.infrastructure.persistence;

import com.wanted.momocity.enrollment.domain.model.Enrollment;
import com.wanted.momocity.enrollment.domain.repository.EnrollmentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * EnrollmentRepositoryAdapter는 도메인 Repository를 실제 JPA Repository와 연결하는 클래스
 * 도메인 계층의 EnrollmentRepository는 "무슨 기능이 필요한지"만 정의합니다.
 * 이 Adapter는 그 기능을 실제 DB 접근 코드로 구현합니다.
 */
@Repository
@RequiredArgsConstructor
public class EnrollmentRepositoryAdapter implements EnrollmentRepository {

    // 실제 DB 접근을 담당하는 Spring Data JPA Repository
    private final EnrollmentJpaRepository enrollmentJpaRepository;

    /*
     * 수강신청 정보를 저장
     * 처리 흐름:
     * 1. Enrollment 도메인 객체를 EnrollmentJpaEntity로 변환
     * 2. JPA Repository로 저장
     * 3. 저장된 Entity를 다시 Enrollment 도메인 객체로 변환
     */
    @Override
    public Enrollment save(Enrollment enrollment) {
        EnrollmentJpaEntity entity = EnrollmentJpaEntity.from(enrollment);

        EnrollmentJpaEntity savedEntity = enrollmentJpaRepository.save(entity);

        return savedEntity.toDomain();
    }

    /*
     * 특정 사용자가 특정 강의를 이미 수강신청했는지 확인
     * 중복 수강신청 방지에 사용
     */
    @Override
    public boolean existsByUserIdAndLectureId(Long userId, Long lectureId) {
        return enrollmentJpaRepository.existsByUserIdAndLectureId(userId, lectureId);
    }

    /*
     * 특정 사용자의 특정 강의 수강신청 정보를 조회
     * viewing 쪽에서 수강 여부 확인에 사용할 수 있음
     */
    @Override
    public Optional<Enrollment> findByUserIdAndLectureId(Long userId, Long lectureId) {
        return enrollmentJpaRepository.findByUserIdAndLectureId(userId, lectureId)
                .map(EnrollmentJpaEntity::toDomain);
    }

    /*
     * 특정 사용자의 전체 수강신청 목록을 조회
     * 나중에 내 수강 내역 조회에서 사용
     */
    @Override
    public List<Enrollment> findAllByUserId(Long userId) {
        return enrollmentJpaRepository.findAllByUserId(userId)
                .stream()
                .map(EnrollmentJpaEntity::toDomain)
                .toList();
    }
}