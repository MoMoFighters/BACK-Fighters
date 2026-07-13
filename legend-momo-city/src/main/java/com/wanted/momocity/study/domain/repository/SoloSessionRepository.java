package com.wanted.momocity.study.domain.repository;

import com.wanted.momocity.study.domain.model.SoloSession;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/*
 * comment.
 *  SoloSession 도메인 저장소 인터페이스
 *  - infrastructure 를 모르고 도메인 계층에서만 사용
 *  - 구현체 : SoloSessionRepositoryAdapter
 * */

public interface SoloSessionRepository {

    // 세션 저장 (생성, 수정)
    SoloSession save(SoloSession session);

    // 세션 단건 조회 (id 기준)
    Optional<SoloSession> findById(Long sessionId);

    // 현재 진행 중(RUNNING/PAUSED)인 세션 단건 조회
    // "GET /solo/current" 및 동시 타이머 검증(다른 세션과 겹치는지)에 사용
    Optional<SoloSession> findActiveByUserId(Long userId);

    // 유저의 솔로 세션 이력 조회 (ENDED 포함 전체, 최신순)
    // "GET /solo/history" 용
    List<SoloSession> findByUserIdOrderByStartTimeDesc(Long userId, Long cursor, int size);

    /*
     * comment.
     *  24시간 초과로 아직 강제 종료되지 않은 세션 목록 조회
     *  - 스케줄러(StudyCleanupScheduler 또는 별도)가 주기적으로 조회해서 forceEnd 처리할 때 사용
     *  - status IN (RUNNING, PAUSED) AND startTime < now - 24h
     * */
    List<SoloSession> findExpiredActiveSessions(LocalDateTime threshold);

}