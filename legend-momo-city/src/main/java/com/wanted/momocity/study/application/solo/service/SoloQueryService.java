package com.wanted.momocity.study.application.solo.service;

import com.wanted.momocity.study.application.solo.usecase.SoloQueryUseCase;
import com.wanted.momocity.study.domain.model.SoloSession;
import com.wanted.momocity.study.domain.repository.SoloSessionRepository;
import com.wanted.momocity.study.presentation.api.response.SoloCurrentResponse;
import com.wanted.momocity.study.presentation.api.response.SoloHistoryResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Optional;

/*
 * comment.
 *  솔로 세션 읽기 작업 UseCase 구현체
 *  - 현재 진행 중인 세션 조회, 세션 이력 조회
 * */

@Slf4j
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class SoloQueryService implements SoloQueryUseCase {

    private final SoloSessionRepository soloSessionRepository;

    /*
     * comment.
     *  현재 진행 중인 솔로 세션 조회
     *  -
     *  accumulatedSeconds는 DB에 저장된 totalSeconds 그대로가 아닌 RUNNING 상태라면
     *  lastResumedAt부터 지금까지의 경과시간을 더해서 "실시간에 가까운 값"을 계산해 내려준다.
     *  (DB의 totalSeconds는 pause/end 시점에만 확정 -> RUNNING 도중 새로고침시 실제 경과시간보다 적게 보이는 문제 발생
     *  PAUSED 상태라면 lastResumedAt이 null -> DB의 totalSeconds를 그대로 사용
     * */
    @Override
    public Optional<SoloCurrentResponse> getCurrent(Long userId) {

        return soloSessionRepository.findActiveByUserId(userId)
                .map(session -> {
                    int liveSeconds = calculateLiveSeconds(session);
                    log.info("[Study] 현재 진행 중인 솔로 세션 조회 완료 | userId={}, sessionId={}",
                            userId, session.getId());
                    return new SoloCurrentResponse(
                            session.getId(), session.getStatus().name(),
                            session.getStartTime(), liveSeconds
                    );
                });
    }

    // 솔로 세션 이력 조회 (ENDED 포함 전체 - 최신순, 커서 기반)
    @Override
    public SoloHistoryResponse getHistory(Long userId, Long cursor, int size) {

        var sessions = soloSessionRepository.findByUserIdOrderByStartTimeDesc(userId, cursor, size);

        var items = sessions.stream()
                .map(session -> new SoloHistoryResponse.SessionItem(
                        session.getId(), session.getStartTime(), session.getEndTime(), session.getTotalSeconds()
                ))
                .toList();

        log.info("[Study] 솔로 세션 이력 조회 완료 | userId={}, count={}", userId, items.size());
        return new SoloHistoryResponse(items);
    }

    // RUNNING이면 실시간 경과시간을 더하고, PAUSED면 저장된 값을 그대로 반환
    private int calculateLiveSeconds(SoloSession session) {
        if (session.getLastResumedAt() == null) {
            return session.getTotalSeconds();
        }
        long elapsed = Duration.between(session.getLastResumedAt(), LocalDateTime.now()).getSeconds();
        return session.getTotalSeconds() + (int) Math.max(elapsed, 0);
    }

}
