package com.wanted.momocity.study.infrastructure.adapter;

import com.wanted.momocity.study.domain.model.SoloSession;
import com.wanted.momocity.study.domain.repository.SoloSessionRepository;
import com.wanted.momocity.study.infrastructure.persistence.SoloSessionJpaEntity;
import com.wanted.momocity.study.infrastructure.persistence.SoloSessionJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/*
 * comment.
 *  SoloSessionRepository 인터페이스 구현체
 *  -> domain.repository 인터페이스 <- 구현 -> JpaRepository 연결
 * */

@Component
@RequiredArgsConstructor
public class SoloSessionRepositoryAdapter implements SoloSessionRepository {

    private static final List<SoloSession.SoloSessionStatus> ACTIVE_STATUSES =
            List.of(SoloSession.SoloSessionStatus.RUNNING, SoloSession.SoloSessionStatus.PAUSED);

    private final SoloSessionJpaRepository soloSessionJpaRepository;

    // 세션 저장 (생성, 수정)
    @Override
    public SoloSession save(SoloSession session) {
        return soloSessionJpaRepository.save(SoloSessionJpaEntity.from(session)).toDomain();
    }

    // 세션 단건 조회 (id 기준)
    @Override
    public Optional<SoloSession> findById(Long sessionId) {
        return soloSessionJpaRepository.findById(sessionId)
                .map(SoloSessionJpaEntity::toDomain);
    }

    // 현재 진행 중(RUNNING/PAUSED)인 세션 단건 조회
    @Override
    public Optional<SoloSession> findActiveByUserId(Long userId) {
        return soloSessionJpaRepository.findActiveByUserId(userId, ACTIVE_STATUSES)
                .map(SoloSessionJpaEntity::toDomain);
    }

    // 유저의 솔로 세션 이력 조회 (ENDED 포함 전체, 최신순)
    // size+1개 조회 후 다음 페이지 존재 여부 확인하는 패턴은 PostRepositoryAdapter와 동일
    @Override
    public List<SoloSession> findByUserIdOrderByStartTimeDesc(Long userId, Long cursor, int size) {
        return soloSessionJpaRepository.findByUserIdWithCursor(userId, cursor, PageRequest.of(0, size + 1))
                .stream()
                .map(SoloSessionJpaEntity::toDomain)
                .toList();
    }

    // 24시간 초과로 아직 강제 종료되지 않은 세션 목록 조회 (스케줄러용)
    @Override
    public List<SoloSession> findExpiredActiveSessions(LocalDateTime threshold) {
        return soloSessionJpaRepository.findExpiredActiveSessions(ACTIVE_STATUSES, threshold)
                .stream()
                .map(SoloSessionJpaEntity::toDomain)
                .toList();
    }
}

