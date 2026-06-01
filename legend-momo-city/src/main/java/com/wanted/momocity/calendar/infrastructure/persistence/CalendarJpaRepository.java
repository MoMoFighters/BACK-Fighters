package com.wanted.momocity.calendar.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

/*
 * comment.
 *  Spring Data JPA 가 구현체 자동 생성
 *  Domain 모름, JpaEntity 만 다룸
 *  실제 DB 쿼리는 해당 클래스에서 실행
 * */

public interface CalendarJpaRepository extends JpaRepository<CalendarJpaEntity, Long> {


     // 월별 캘린더 조회 쿼리
     // Todo: start 가 해당 월 범위 안에 있는 것
     // Memo: start ~ end 가 해당 월과 겹치는 것
     //       (start <= endDate AND (end >= startDate OR end IS NULL))
    @Query("""
    SELECT c FROM CalendarJpaEntity c
    WHERE c.userId = :userId
    AND (
        (c.category = 'TODO' AND c.start BETWEEN :startDate AND :endDate)
        OR
        (c.category = 'MEMO' AND c.start <= :endDate
            AND (c.end >= :startDate OR c.end IS NULL))
    )
    """)
    List<CalendarJpaEntity> findByUserIdAndDateBetween(
            @Param("userId") Long userId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );

}
