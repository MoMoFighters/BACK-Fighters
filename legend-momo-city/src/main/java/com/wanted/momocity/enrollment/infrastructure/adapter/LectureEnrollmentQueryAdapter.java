package com.wanted.momocity.enrollment.infrastructure.adapter;

import com.wanted.momocity.enrollment.infrastructure.persistence.EnrollmentJpaEntity;
import com.wanted.momocity.enrollment.infrastructure.persistence.EnrollmentJpaRepository;
import com.wanted.momocity.lecture.application.port.LectureEnrollmentQueryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

/**
 * LectureEnrollmentQueryAdapter는 lecture 목록 조회에서 필요한
 * enrollment 정보를 제공하는 Adapter
 * lecture 패키지는 enrollment 테이블을 직접 알지 않고,
 * LectureEnrollmentQueryPort를 통해 필요한 정보만 요청
 */
@Component
@RequiredArgsConstructor
public class LectureEnrollmentQueryAdapter implements LectureEnrollmentQueryPort {

    // enrollment 테이블 조회를 담당하는 JPA Repository
    private final EnrollmentJpaRepository enrollmentJpaRepository;

    /**
     * 사용자가 수강 신청한 강의 ID 목록을 조회
     */
    @Override
    public List<Long> findLectureIdsByUserId(Long userId) {
        return enrollmentJpaRepository.findAllByUserId(userId)
                .stream()
                .map(EnrollmentJpaEntity::getLectureId)
                .toList();
    }

    /*
     * 특정 사용자의 특정 강의 수강 신청 정보를 조회
     * 강의 목록 응답에서 totalProgress, completedCount를 채울 때 사용
     */
    @Override
    public Optional<EnrollmentProgress> findByUserIdAndLectureId(
            Long userId,
            Long lectureId
    ) {
        return enrollmentJpaRepository.findByUserIdAndLectureId(userId, lectureId)
                .map(this::toProgress);
    }

    // EnrollmentJpaEntity를 lecture 쪽에서 필요한 진행 정보로 변환
    private EnrollmentProgress toProgress(EnrollmentJpaEntity entity) {
        return new EnrollmentProgress(
                entity.getId(),
                entity.getLectureId(),
                entity.getTotalProgress(),
                entity.getCompletedCount()
        );
    }
}