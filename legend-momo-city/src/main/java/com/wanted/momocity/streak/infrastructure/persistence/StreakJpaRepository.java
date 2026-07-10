package com.wanted.momocity.streak.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/*
* comment.
*  Spring Data JPA 가 구현체를 자동으로 생성해주는 JPA 전용 인터페이스
*  Domain 을 모르고 JpaEntity 만 다룸 -> 실제 DB 쿼리는 여기서 실행됨
* */

public interface StreakJpaRepository extends JpaRepository<StreakJpaEntity, Long> {

    Optional<StreakJpaEntity> findByUserIdAndStreakDate(
            Long userId, LocalDate streakDate
    );

    List<StreakJpaEntity> findByUserIdAndStreakDateBetween(
            Long userId, LocalDate startDate, LocalDate endDate
    );

    @Query("""
    SELECT s FROM StreakJpaEntity s
    WHERE s.userId = :userId
    AND FUNCTION('YEAR', s.streakDate) = :year
    ORDER BY s.streakDate ASC
""")
    List<StreakJpaEntity> findByUserIdAndYear(
            @Param("userId") Long userId,
            @Param("year") int year
    );

}
