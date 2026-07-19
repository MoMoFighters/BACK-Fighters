package com.wanted.momocity.study.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

/*
 * comment.
 *  Spring Data JPA 가 구현체를 자동으로 생성
 *  -> Domain 을 모르고 JpaEntity 만 다룸
 * */

public interface StudyLapJpaRepository extends JpaRepository<StudyLapJpaEntity, Long> {

    /*
     * comment.
     *  특정 세션의 진행 중인(endedAt이 null인) 랩 단건 조회
     *  - roomId는 null일 수 있으므로 IS NULL / = 조건을 분기해야 하는데,
     *    JPQL에서 :roomId가 null이면 자동으로 IS NULL 비교 불가
     *    -> room_id의 null 여부에 따라 분기 조건 명시
     * */

    @Query("""
        SELECT l FROM StudyLapJpaEntity l
        WHERE l.sessionId = :sessionId
        AND ((:roomId IS NULL AND l.roomId IS NULL) OR l.roomId = :roomId)
        AND l.endedAt IS NULL
    """)
    Optional<StudyLapJpaEntity> findOngoingBySessionId(
            @Param("roomId") Long roomId,
            @Param("sessionId") Long sessionId
    );

    // 특정 세션의 전체 랩 목록 조회 (시작 순서대로)
    @Query("""
        SELECT l FROM StudyLapJpaEntity l
        WHERE l.sessionId = :sessionId
        AND ((:roomId IS NULL AND l.roomId IS NULL) OR l.roomId = :roomId)
        ORDER BY l.startedAt ASC
    """)
    List<StudyLapJpaEntity> findAllBySessionIdOrderByStartedAtAsc(
            @Param("roomId") Long roomId,
            @Param("sessionId") Long sessionId
    );

    /*
     * comment.
     *  특정 세션의 전체 랩 개수 조회 (COUNT 쿼리)
     *  - room_id가 null일 수 있으므로 다른 조회 메서드들과 동일하게
     *    :roomId IS NULL 분기 조건을 그대로 사용
     * */

    @Query("""
    SELECT COUNT(l) FROM StudyLapJpaEntity l
    WHERE l.sessionId = :sessionId
    AND ((:roomId IS NULL AND l.roomId IS NULL) OR l.roomId = :roomId)
""")
    long countBySessionId(
            @Param("roomId") Long roomId,
            @Param("sessionId") Long sessionId
    );

    // 특정 방의 랩 기록 전체 삭제 (하드딜리트 시 study_lap은 FK가 없어 직접 지워야 함)
    @org.springframework.data.jpa.repository.Modifying
    @Query("DELETE FROM StudyLapJpaEntity l WHERE l.roomId = :roomId")
    void deleteAllByRoomId(@Param("roomId") Long roomId);

}
