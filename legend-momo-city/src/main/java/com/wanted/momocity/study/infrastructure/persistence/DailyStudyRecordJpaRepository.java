package com.wanted.momocity.study.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/*
 * comment.
 *  Spring Data JPA 가 구현체를 자동으로 생성
 *  -> Domain 을 모르고 JpaEntity 만 다룸
 * */

public interface DailyStudyRecordJpaRepository extends JpaRepository<DailyStudyRecordJpaEntity, Long> {

    // 특정 유저 + 특정 날짜 record 단건 조회 - 이벤트 리스너가 누적할 대상을 찾을 때 사용
    Optional<DailyStudyRecordJpaEntity> findByUserIdAndStudyDate(Long userId, LocalDate studyDate);

    /*
     * comment.
     *  특정 유저의 1년치 잔디 데이터 조회 ("GET /records/yearly" 용, 마이페이지 진입 시)
     *  - year 기준으로 해당 연도의 1/1 ~ 12/31 범위를 BETWEEN으로 조회
     * */
    @Query("""
        SELECT r FROM DailyStudyRecordJpaEntity r
        WHERE r.userId = :userId
        AND r.studyDate BETWEEN :startDate AND :endDate
        ORDER BY r.studyDate ASC
    """)
    List<DailyStudyRecordJpaEntity> findAllByUserIdAndDateRange(
            @Param("userId") Long userId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );

    /*
     * comment.
     *  여러 유저의 특정 날짜 record를 한 번에 조회 (방 일별 랭킹 조회 시 N+1 방지)
     *  - IN 절로 벌크 조회, PostRepositoryAdapter의 findPostIdsByUserId 등과 동일한 패턴
     * */
    List<DailyStudyRecordJpaEntity> findAllByUserIdInAndStudyDate(List<Long> userIds, LocalDate studyDate);

    /*
     * comment.
     *  INSERT ... ON DUPLICATE KEY UPDATE를 사용한 원자적 증분 upsert
     *  - uq_daily_study_record (user_id, study_date) 유니크 제약을 활용
     *  - 없으면 새로 만들고(seconds로 초기화), 있으면 기존 total_seconds에 seconds를 더함
     *  - 이 연산 자체가 DB 레벨에서 원자적으로 처리되므로 동시 호출에도 값이 유실되지 않음
     * */
    @Modifying
    @Query(value = """
    INSERT INTO daily_study_record (user_id, study_date, total_seconds, created_at, updated_at)
    VALUES (:userId, :studyDate, :seconds, NOW(6), NOW(6))
    ON DUPLICATE KEY UPDATE
        total_seconds = total_seconds + VALUES(total_seconds),
        updated_at = NOW(6)
    """, nativeQuery = true)
    void incrementSeconds(
            @Param("userId") Long userId,
            @Param("studyDate") LocalDate studyDate,
            @Param("seconds") int seconds
    );


}
