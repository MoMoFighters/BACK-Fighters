package com.wanted.momocity.review.infrastructure.persistence;

import org.springframework.data.repository.query.Param;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface ReviewJpaRepository extends JpaRepository<ReviewJpaEntity, Long> {

    // 사용자가 특정 강의에 이미 수강평을 작성했는지 확인
    boolean existsByUserIdAndLectureId(
            Long userId,
            Long lectureId
    );

    // 해당 강의의 rating 평균을 구하고 없으면 0 반환
    @Query("select coalesce(avg(r.rating), 0) " +
            "from ReviewJpaEntity r where r.lectureId = :lectureId")
    // 특정 강의의 평균 평점 조회
    double findAverageRatingByLectureId(@Param("lectureId") Long lectureId);

    // 특정 강의 수강평을 최신순
    Page<ReviewJpaEntity> findAllByLectureIdOrderByCreatedAtDesc(Long lectureId, Pageable pageable);

    long countByLectureId(Long lectureId);
}
