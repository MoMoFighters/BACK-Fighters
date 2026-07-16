package com.wanted.momocity.study.domain.repository;

import com.wanted.momocity.study.domain.model.StudyLap;

import java.util.List;
import java.util.Optional;

/*
 * comment.
 *  StudyLap 도메인 저장소 인터페이스
 *  - infrastructure 를 모르고 도메인 계층에서만 사용
 *  - 구현체 : StudyLapRepositoryAdapter
 * */

public interface StudyLapRepository {

    // 랩 저장 (생성, 마감 시 수정)
    StudyLap save(StudyLap lap);

    /*
     * comment.
     *  특정 세션의 진행 중인(endedAt = null) 랩 단건 조회
     *  - pause/end 시점에 "지금 마감해야 할 랩이 어떤 건지" 찾을 때 사용
     *  - roomId가 null이면 솔로, 있으면 그룹방 - sessionId만으로 이미 유일하게 특정
     *    -> roomId는 조회 조건에 필수는 아니지만 명시적으로 함께 받아 실수를 방지
     * */
    Optional<StudyLap> findOngoingBySessionId(Long roomId, Long sessionId);

    /*
     * comment.
     *  특정 세션의 전체 랩 목록 조회 (시작 순서대로)
     *  - "GET /solo/laps", "GET /rooms/{roomId}/members/{targetUserId}/laps" 양쪽에서 사용
     * */
    List<StudyLap> findAllBySessionIdOrderByStartedAtAsc(Long roomId, Long sessionId);

    // 특정 세션의 전체 랩 개수 조회 (lapNumber 계산용)
    long countBySessionId(Long roomId, Long sessionId);

}
