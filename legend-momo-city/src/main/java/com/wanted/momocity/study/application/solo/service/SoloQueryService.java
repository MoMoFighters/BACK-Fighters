package com.wanted.momocity.study.application.solo.service;

import com.wanted.momocity.study.application.common.service.StudyLapService;
import com.wanted.momocity.study.application.solo.usecase.SoloQueryUseCase;
import com.wanted.momocity.study.domain.exception.StudyNotFoundException;
import com.wanted.momocity.study.domain.model.SoloSession;
import com.wanted.momocity.study.domain.repository.SoloSessionRepository;
import com.wanted.momocity.study.presentation.api.response.common.LapItem;
import com.wanted.momocity.study.presentation.api.response.common.SoloLapListResponse;
import com.wanted.momocity.study.presentation.api.response.solo.SoloCurrentResponse;
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
    private final StudyLapService studyLapService;

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

    /*
     * comment.
     *  현재(또는 가장 최근) 솔로 세션의 랩 목록 조회
     *  - findActiveByUserId()로 진행 중인 세션을 우선 찾고, 없으면 404
     *  - 랩 번호(lapNumber)는 startedAt 순서를 그대로 index+1로 매긴다
     * */

    @Override
    public SoloLapListResponse getLaps(Long userId) {

        SoloSession session = soloSessionRepository.findActiveByUserId(userId)
                .orElseThrow(() -> new StudyNotFoundException("조회할 세션이 없습니다."));

        var laps = studyLapService.getLaps(null, session.getId());

        var items = java.util.stream.IntStream.range(0, laps.size())
                .mapToObj(i -> {
                    var lap = laps.get(i);
                    return new LapItem(i + 1, lap.getStartedAt(), lap.getEndedAt(), lap.getSeconds());
                })
                .toList();

        log.info("[Study] 솔로 세션 랩 목록 조회 완료 | userId={}, sessionId={}, count={}",
                userId, session.getId(), items.size());
        return new SoloLapListResponse(session.getId(), items);
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
