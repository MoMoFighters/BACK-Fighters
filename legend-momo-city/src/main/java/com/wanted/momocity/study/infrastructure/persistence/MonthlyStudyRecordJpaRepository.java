package com.wanted.momocity.study.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

/*
 * comment.
 *  Spring Data JPA 가 구현체를 자동으로 생성
 *  -> Domain 을 모르고 JpaEntity 만 다룸
 * */

public interface MonthlyStudyRecordJpaRepository extends JpaRepository<MonthlyStudyRecordJpaEntity, Long> {

    // 특정 유저 + 특정 년월 record 단건 조회 - 이벤트 리스너가 누적할 대상을 찾을 때 사용
    Optional<MonthlyStudyRecordJpaEntity> findByUserIdAndYearMonth(Long userId, String yearMonth);

    // 여러 유저의 특정 년월 record를 한 번에 조회 (방 월별 랭킹 조회 시 N+1 방지)
    List<MonthlyStudyRecordJpaEntity> findAllByUserIdInAndYearMonth(List<Long> userIds, String yearMonth);

    /*
     * comment.
     *  INSERT ... ON DUPLICATE KEY UPDATE를 사용한 원자적 증분 upsert
     *  - uq_daily_study_record (user_id, study_date) 유니크 제약을 활용
     *  - 없으면 새로 만들고(seconds로 초기화), 있으면 기존 total_seconds에 seconds를 더함
     *  - 이 연산 자체가 DB 레벨에서 원자적으로 처리되므로 동시 호출에도 값이 유실되지 않음
     * */
    @Modifying
    @Query(value = """
    INSERT INTO monthly_study_record (user_id, year_month, total_seconds, created_at, updated_at)
    VALUES (:userId, :yearMonth, :seconds, NOW(6), NOW(6))
    ON DUPLICATE KEY UPDATE
        total_seconds = total_seconds + VALUES(total_seconds),
        updated_at = NOW(6)
    """, nativeQuery = true)
    void incrementSeconds(
            @Param("userId") Long userId,
            @Param("yearMonth") String yearMonth,
            @Param("seconds") int seconds
    );

}
