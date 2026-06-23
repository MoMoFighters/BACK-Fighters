package com.wanted.momocity.review.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

public interface ReviewJpaRepository extends JpaRepository<ReviewJpaEntity, Long> {

    // 사용자가 특정 강의에 이미 수강평을 작성했는지 확인
    boolean existsByUserIdAndLectureId(
            Long userId,
            Long lectureId
    );
}
