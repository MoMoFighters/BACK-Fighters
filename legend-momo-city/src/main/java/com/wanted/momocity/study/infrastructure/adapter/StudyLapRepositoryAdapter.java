package com.wanted.momocity.study.infrastructure.adapter;

import com.wanted.momocity.study.domain.model.StudyLap;
import com.wanted.momocity.study.domain.repository.StudyLapRepository;
import com.wanted.momocity.study.infrastructure.persistence.StudyLapJpaEntity;
import com.wanted.momocity.study.infrastructure.persistence.StudyLapJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

/*
 * comment.
 *  StudyLapRepository 인터페이스 구현체
 *  -> domain.repository 인터페이스 <- 구현 -> JpaRepository 연결
 * */

@Component
@RequiredArgsConstructor
public class StudyLapRepositoryAdapter implements StudyLapRepository {

    private final StudyLapJpaRepository studyLapJpaRepository;

    // 랩 저장 (생성, 마감 시 수정)
    @Override
    public StudyLap save(StudyLap lap) {
        return studyLapJpaRepository.save(StudyLapJpaEntity.from(lap)).toDomain();
    }

    // 특정 세션의 진행 중인 랩 단건 조회
    @Override
    public Optional<StudyLap> findOngoingBySessionId(Long roomId, Long sessionId) {
        return studyLapJpaRepository.findOngoingBySessionId(roomId, sessionId)
                .map(StudyLapJpaEntity::toDomain);
    }

    // 특정 세션의 전체 랩 목록 조회 (시작 순서대로)
    @Override
    public List<StudyLap> findAllBySessionIdOrderByStartedAtAsc(Long roomId, Long sessionId) {
        return studyLapJpaRepository.findAllBySessionIdOrderByStartedAtAsc(roomId, sessionId)
                .stream()
                .map(StudyLapJpaEntity::toDomain)
                .toList();
    }

    // 특정 세션의 전체 랩 개수 조회 (lapNumber 계산용, COUNT 쿼리라 가벼움)
    @Override
    public long countBySessionId(Long roomId, Long sessionId) {
        return studyLapJpaRepository.countBySessionId(roomId, sessionId);
    }

    // 그룹방 삭제시 하드 딜리트 스케줄러
    @Override
    public void deleteAllByRoomId(Long roomId) {
        studyLapJpaRepository.deleteAllByRoomId(roomId);
    }

}
