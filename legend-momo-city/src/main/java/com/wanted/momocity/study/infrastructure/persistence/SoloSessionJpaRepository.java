package com.wanted.momocity.study.infrastructure.persistence;

import com.wanted.momocity.study.domain.model.SoloSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/*
 * comment.
 *  Spring Data JPA 가 구현체를 자동으로 생성
 *  -> Domain 을 모르고 JpaEntity 만 다룸
 * */

public interface SoloSessionJpaRepository extends JpaRepository<SoloSessionJpaEntity, Long> {

    /*
     * comment.
     *  현재 진행 중(RUNNING 또는 PAUSED)인 세션 단건 조회
     *  - "GET /solo/current" 화면 복구용, 그리고 member 도메인의 동시 타이머 검증시
     *    "이 유저가 솔로 세션을 이미 갖고 있는지" 확인할 때도 사용
     *  - status가 두 가지(RUNNING, PAUSED) 중 하나인 걸 IN 절로 조회
     * */
    @Query("""
        SELECT s FROM SoloSessionJpaEntity s
        WHERE s.userId = :userId
        AND s.status IN :statuses
    """)
    Optional<SoloSessionJpaEntity> findActiveByUserId(
            @Param("userId") Long userId,
            @Param("statuses") List<SoloSession.SoloSessionStatus> statuses
    );

    // 유저의 솔로 세션 이력 조회 (ENDED 포함 전체, 최신순) - "GET /solo/history" 용
    // cursor가 null이면 첫 페이지, 아니면 해당 id보다 작은 데이터만 조회
    @Query("""
        SELECT s FROM SoloSessionJpaEntity s
        WHERE s.userId = :userId
        AND (:cursor IS NULL OR s.id < :cursor)
        ORDER BY s.id DESC
    """)
    List<SoloSessionJpaEntity> findByUserIdWithCursor(
            @Param("userId") Long userId,
            @Param("cursor") Long cursor,
            org.springframework.data.domain.Pageable pageable
    );

    /*
     * comment.
     *  24시간 초과로 아직 강제 종료되지 않은 세션 목록 조회
     *  - 스케줄러가 주기적으로 조회해서 forceEndByTimeout() 처리할 때 사용
     *  - status가 RUNNING 또는 PAUSED이면서, startTime이 threshold(현재시각-24시간)보다 이전인 것
     * */
    @Query("""
        SELECT s FROM SoloSessionJpaEntity s
        WHERE s.status IN :statuses
        AND s.startTime < :threshold
    """)
    List<SoloSessionJpaEntity> findExpiredActiveSessions(
            @Param("statuses") List<SoloSession.SoloSessionStatus> statuses,
            @Param("threshold") LocalDateTime threshold
    );

}
