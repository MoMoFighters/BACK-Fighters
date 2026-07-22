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

    // 스케줄러용 - 전체 유저 오늘 날짜 포함 일정 조회
    // Todo  : start = today AND 아직 미완료(isCompleted = false)인 것만 (완료한 건 알림 필요 없음)
    // Memo  : start <= today <= end (end가 null이면 start 당일만 있는 일정으로 간주)
    @Query("""
    SELECT c FROM CalendarJpaEntity c
    WHERE (
        (c.category = 'TODO' AND c.start = :date AND c.isCompleted = false)
        OR
        (c.category = 'MEMO' AND c.start <= :date
            AND (c.end >= :date OR c.end IS NULL))
    )
    """)
    List<CalendarJpaEntity> findAllNotificationTargetsByDate(@Param("date") LocalDate date);

}
