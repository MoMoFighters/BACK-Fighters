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

    // 날짜별 조회
    // Todo / Memo 날짜 필터링 조건이 달라서 네이밍 규칙으로 표현 불가
    // Todo : start = date
    // Memo : start <= date AND (end >= date OR end IS NULL)
    @Query("""
            SELECT c FROM CalendarJpaEntity c
            WHERE c.userId = :userId
            AND (
                (c.category = 'TODO' AND c.start = :date)
                OR
                (c.category = 'MEMO' AND c.start <= :date
                AND (c.end >= :date OR c.end IS NULL))
            )
            """)
    List<CalendarJpaEntity> findByUserIdAndDate(
            @Param("userId") Long userId,
            @Param("date") LocalDate date
    );

}
