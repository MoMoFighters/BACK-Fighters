package com.wanted.momocity.study.application.solo.service;

import com.wanted.momocity.global.domain.common.exception.DomainRuleViolationException;
import com.wanted.momocity.study.application.common.service.StudyLapService;
import com.wanted.momocity.study.application.solo.result.SoloActionResult;
import com.wanted.momocity.study.application.solo.usecase.SoloCommandUseCase;
import com.wanted.momocity.study.domain.event.StudySessionAccumulatedEvent;
import com.wanted.momocity.study.domain.exception.StudyNotFoundException;
import com.wanted.momocity.study.domain.model.SoloSession;
import com.wanted.momocity.study.domain.model.StudyLap;
import com.wanted.momocity.study.domain.repository.GroupRoomMemberRepository;
import com.wanted.momocity.study.domain.repository.SoloSessionRepository;
import com.wanted.momocity.study.presentation.api.response.common.LapItem;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;

/*
 * comment.
 *  솔로 세션 쓰기 작업 UseCase 구현체
 *  - 시작(재개 포함)/일시정지/종료
 *  -
 *  SoloSession.MAX_DURATION_SECONDS 같은 상수를 도메인에 두지 않고 24시간 직접 검증
 * */

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class SoloCommandService implements SoloCommandUseCase {

    // 최대 세션 지속시간 24시간 - 재개 시 이 시간을 넘겼으면 재개를 거부한다
    private static final long MAX_DURATION_SECONDS = 24 * 60 * 60L;

    private final SoloSessionRepository soloSessionRepository;
    private final GroupRoomMemberRepository groupRoomMemberRepository;
    private final StudyLapService studyLapService;
    private final ApplicationEventPublisher eventPublisher;

    /*
     * comment.
     *  솔로 세션 시작 (신규 시작 + 일시정지 후 재개 통합)
     *  -
     *  분기 로직 :
     *  1. 기존에 PAUSED 상태인 세션 존재 -> 그걸 재개(wasResumed=true)
     *     단, 24시간이 이미 지났으면 재개를 막고 세션 만료 예외 처리
     *  2. RUNNING 상태인 세션 존재 -> 이미 진행 중이므로 예외 처리
     *  3. 아무 세션도 없으면 -> 새로 생성(wasResumed=false)
     *  -
     *  동시 활성화 검증(validateNoOtherActiveTimer)은 그룹방 타이머와의 충돌도 함께 확인
     * */
    @Override
    public SoloActionResult start(Long userId) {

        var existing = soloSessionRepository.findActiveByUserId(userId);

        if (existing.isPresent() && existing.get().isRunning()) {
            throw new DomainRuleViolationException("이미 다른 곳에서 진행 중인 타이머가 있습니다.");
        }

        validateNoOtherActiveTimer(userId);

        LocalDateTime now = LocalDateTime.now();

        if (existing.isPresent()) {
            // 재개 케이스 - 24시간 초과 여부 먼저 확인
            SoloSession session = existing.get();
            long elapsedFromStart = Duration.between(session.getStartTime(), now).getSeconds();
            if (elapsedFromStart >= MAX_DURATION_SECONDS) {
                throw new DomainRuleViolationException("세션이 만료되어 재개할 수 없습니다.");
            }
            session.resume(now);
            SoloSession saved = soloSessionRepository.save(session);

            StudyLap newLap = studyLapService.startLap(userId, null, saved.getId(), now);
            int lapNumber = (int) studyLapService.countLaps(null, saved.getId());

            log.info("[Study] 솔로 세션 재개 | userId={}, sessionId={}", userId, saved.getId());
            return SoloActionResult.ofStarted(saved, true, toLapItem(newLap, lapNumber));
        }

        // 신규 시작 케이스
        SoloSession newSession = SoloSession.create(userId, now);
        SoloSession saved = soloSessionRepository.save(newSession);

        StudyLap newLap = studyLapService.startLap(userId, null, saved.getId(), now);

        log.info("[Study] 솔로 세션 시작 | userId={}, sessionId={}", userId, saved.getId());
        return SoloActionResult.ofStarted(saved, false, toLapItem(newLap, 1));
    }

    // 솔로 세션 일시정지 - 누적 시간 확정 후 상태 전환
    @Override
    public SoloActionResult pause(Long userId) {

        SoloSession session = getActiveSession(userId);
        if (!session.isRunning()) {
            throw new DomainRuleViolationException("진행 중인 타이머가 없습니다.");
        }

        int increment = accumulateElapsed(session);
        session.pause();
        SoloSession saved = soloSessionRepository.save(session);

        if(increment > 0) {
            eventPublisher.publishEvent(
                    new StudySessionAccumulatedEvent(userId, LocalDate.now(), increment)
            );
        }

        LocalDateTime now = LocalDateTime.now();
        StudyLap closedLap = studyLapService.closeLap(null, saved.getId(), now);
        int lapNumber = (int) studyLapService.countLaps(null, saved.getId());

        log.info("[Study] 솔로 세션 일시정지 | userId={}, sessionId={}", userId, saved.getId());
        return SoloActionResult.ofPaused(saved, toLapItem(closedLap, lapNumber));
    }

    /*
     * comment.
     *  솔로 세션 종료 (최종 확정)
     *  종료 시점에 StudySessionEndedEvent를 발행 -> DailyStudyRecord/MonthlyStudyRecord에 반영
     *  자정 분할 로직은 아직 여기 반영되어 있지 않고, 우선 단순화하여 하루치로 발행
     *  (추후 보완 필요, member.timer.TimerCommandService.end()와 동일한 상황)
     * */
    @Override
    public SoloActionResult end(Long userId) {

        SoloSession session = getActiveSession(userId);

        int increment = 0;
        if (session.isRunning()) {
            increment = accumulateElapsed(session);
        }
        session.end(LocalDateTime.now());
        SoloSession saved = soloSessionRepository.save(session);

        LocalDateTime now = LocalDateTime.now();
        // 세션이 RUNNING이 아니라 PAUSED 상태에서 end()가 호출된 경우, 마감할 진행 중인 랩이
        // 없을 수 있다 (이미 pause 시점에 마감됨) - closeLap()은 이 경우 null을 반환하므로 안전하다
        StudyLap closedLap = studyLapService.closeLap(null, saved.getId(), now);
        int lapNumber = (int) studyLapService.countLaps(null, saved.getId());

        if (increment > 0) {
            eventPublisher.publishEvent(
                    new StudySessionAccumulatedEvent(userId, now.toLocalDate(), increment)
            );
        }

        log.info("[Study] 솔로 세션 종료 | userId={}, sessionId={}, increment={}",
                userId, saved.getId(), increment);
        return SoloActionResult.ofEnded(saved, closedLap == null ? null : toLapItem(closedLap, lapNumber));
    }

    // ===== 내부 헬퍼 =====

    // 진행 중(RUNNING/PAUSED)인 세션 조회 - 없으면 404
    private SoloSession getActiveSession(Long userId) {
        return soloSessionRepository.findActiveByUserId(userId)
                .orElseThrow(() -> new StudyNotFoundException("진행 중인 세션이 없습니다."));
    }

    /*
     * comment.
     *  유저가 그룹방에서 이미 타이머를 진행 중인지 검증 (동시 활성화 금지 정책)
     * */
    private void validateNoOtherActiveTimer(Long userId) {
        var studyingElsewhere = groupRoomMemberRepository.findAllByUserIdAndStudying(userId);
        if (!studyingElsewhere.isEmpty()) {
            throw new DomainRuleViolationException("이미 다른 곳에서 진행 중인 타이머가 있습니다.");
        }
    }

    // lastResumedAt ~ now 구간 경과 시간을 계산해서 누적
    private int accumulateElapsed(SoloSession session) {
        if (session.getLastResumedAt() == null) {
            return 0;
        }
        long elapsed = Duration.between(session.getLastResumedAt(), LocalDateTime.now()).getSeconds();
        int increment = (int) Math.max(elapsed, 0);
        session.accumulateSeconds(increment);
        return increment;
    }

    // StudyLap(도메인) -> LapItem(Response) 변환 + 순번 매기기
    private LapItem toLapItem(StudyLap lap, int lapNumber) {
        return new LapItem(lapNumber, lap.getStartedAt(), lap.getEndedAt(), lap.getSeconds());
    }

}
