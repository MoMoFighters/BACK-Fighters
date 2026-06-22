package com.wanted.momocity.enrollment.infrastructure.persistence;

import com.wanted.momocity.lecture.domain.model.LectureCategory;
import io.lettuce.core.dynamic.annotation.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

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

    // 사용자가 수강신청한 강의 중, 특정 카테고리이고 진척도가 100 이상인 강의 수를 조회합니다.
    @Query("""
        select count(enrollment)
        from EnrollmentJpaEntity enrollment
        join LectureJpaEntity lecture on lecture.id = enrollment.lectureId
        where enrollment.userId = :userId
          and lecture.category = :category
          and enrollment.totalProgress >= 100
        """)
    // count 결과는 long으로 나올 수 있으므로 long으로 받습니다.
        long countCompletedLecturesByUserIdAndCategory(
            // 조회할 사용자 ID입니다.
            @Param("userId") Long userId,

            // 조회할 강의 카테고리입니다.
            @Param("category") LectureCategory category
    );
}