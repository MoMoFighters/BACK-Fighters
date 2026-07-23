package com.wanted.momocity.study.application.member.timer.service;

import com.wanted.momocity.study.application.common.service.StudyLapService;
import com.wanted.momocity.study.application.common.util.MidnightSplitter;
import com.wanted.momocity.study.application.member.timer.result.TimerActionResult;
import com.wanted.momocity.study.application.member.timer.usecase.TimerCommandUseCase;
import com.wanted.momocity.study.domain.event.StudySessionAccumulatedEvent;
import com.wanted.momocity.study.domain.event.TimerStatusChangedEvent;
import com.wanted.momocity.study.domain.exception.StudyAccessDeniedException;
import com.wanted.momocity.study.domain.model.GroupRoomMember;
import com.wanted.momocity.study.domain.model.SoloSession;
import com.wanted.momocity.study.domain.model.StudyLap;
import com.wanted.momocity.study.domain.repository.GroupRoomMemberRepository;
import com.wanted.momocity.study.domain.repository.SoloSessionRepository;
import com.wanted.momocity.global.domain.common.exception.DomainRuleViolationException;
import com.wanted.momocity.study.presentation.api.response.common.LapItem;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;

/*
 * comment.
 *  그룹방 내 개인 타이머 쓰기 작업 UseCase 구현체
 * */

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class TimerCommandService implements TimerCommandUseCase {

    // 최대 세션 지속시간 24시간 - SoloCommandService와 동일한 상수/정책
    private static final long MAX_DURATION_SECONDS = 24 * 60 * 60L;

    private final GroupRoomMemberRepository groupRoomMemberRepository;
    private final SoloSessionRepository soloSessionRepository;
    private final StudyLapService studyLapService;
    private final ApplicationEventPublisher eventPublisher;

    /*
     * comment.
     *  타이머 시작 (신규 시작 + 일시정지 후 재개 통합)
     *  wasResumed는 응답의 action(STARTED/RESUMED)을 결정하기 위해 호출 전 상태를 미리 저장.
     *  (member.startTimer() 호출 후에는 timerStatus가 STUDYING으로 바뀌어 구분이 불가능)
     * */

    @Override
    public TimerActionResult start(Long userId, Long roomId) {

        GroupRoomMember member = getJoinedMember(userId, roomId);
        boolean wasResumed = member.getTimerStatus() == GroupRoomMember.TimerStatus.RESTING;

        if (member.getTimerStatus() == GroupRoomMember.TimerStatus.STUDYING) {
            throw new DomainRuleViolationException("이미 다른 곳에서 진행 중인 타이머가 있습니다.");
        }
        validateNoOtherActiveTimer(userId);

        LocalDateTime now = LocalDateTime.now();

        // 재개 케이스일 때만 24시간 초과 여부 확인 (신규 시작은 timerStartedAt이 없으므로 해당 없음)
        // SoloCommandService.start()의 elapsedFromStart 체크와 동일한 로직
        if (wasResumed && member.getTimerStartedAt() != null) {
            long elapsedFromStart = Duration.between(member.getTimerStartedAt(), now).getSeconds();
            if (elapsedFromStart >= MAX_DURATION_SECONDS) {
                member.endTimer();
                groupRoomMemberRepository.save(member);
                log.info("[Study] 24시간 초과 그룹 타이머 자동 만료 처리 | roomId={}, userId={}", roomId, userId);

                // 만료 처리 후, 같은 start() 요청을 "신규 시작"으로 이어서 처리
                wasResumed = false;
            }
        }

        member.startTimer(now);
        GroupRoomMember saved = groupRoomMemberRepository.save(member);

        StudyLap newLap = studyLapService.startLap(userId, roomId, saved.getId(), now);
        int lapNumber = (int) studyLapService.countLaps(roomId, saved.getId());

        eventPublisher.publishEvent(new TimerStatusChangedEvent(roomId, userId, saved.getTimerStatus()));

        log.info("[Study] 그룹 타이머 시작 | roomId={}, userId={}, resumed={}", roomId, userId, wasResumed);
        return TimerActionResult.ofStarted(saved, wasResumed, toLapItem(newLap, lapNumber));
    }

    // 타이머 일시정지 - 누적 시간 확정 후 상태 전환
    @Override
    public TimerActionResult pause(Long userId, Long roomId) {

        GroupRoomMember member = getJoinedMember(userId, roomId);
        if (member.getTimerStatus() != GroupRoomMember.TimerStatus.STUDYING) {
            throw new DomainRuleViolationException("진행 중인 타이머가 없습니다.");
        }

        // 자정 분할을 위해 accumulateElapsed 호출 전에 시작 시각을 미리 확보
        LocalDateTime from = member.getLastResumedAt();
        LocalDateTime now = LocalDateTime.now();

        // pause 시 이번 구간 증분 이벤트 발행
        int increment = accumulateElapsed(member, now);
        member.pauseTimer();
        GroupRoomMember saved = groupRoomMemberRepository.save(member);

        // 단일 이벤트 발행 -> 자정 분할 발행으로 교체
        publishAccumulatedEvents(userId, from, now, increment);

        StudyLap closedLap = studyLapService.closeLap(roomId, saved.getId(), now);
        int lapNumber = (int) studyLapService.countLaps(roomId, saved.getId());

        eventPublisher.publishEvent(new TimerStatusChangedEvent(roomId, userId, saved.getTimerStatus()));

        log.info("[Study] 그룹 타이머 일시정지 | roomId={}, userId={}", roomId, userId);
        return TimerActionResult.ofPaused(saved, toLapItem(closedLap, lapNumber));
    }

    /*
     * comment.
     *  타이머 완전 종료 (방은 유지, timerStatus만 null로 전환)
     *  종료 시점에 StudySessionEndedEvent를 발행해 DailyStudyRecord/MonthlyStudyRecord에 반영
     *  자정 분할 로직은 아직 여기 반영되어 있지 않고, 우선 단순화하여 하루치로 발행 (추후 보완 필요)
     * */

    @Override
    public TimerActionResult end(Long userId, Long roomId) {

        GroupRoomMember member = getJoinedMember(userId, roomId);
        if (member.getTimerStatus() == null) {
            throw new DomainRuleViolationException("진행 중인 타이머가 없습니다.");
        }

        // 자정 분할을 위해 시작 시각 미리 확보 (RESTING이면 from은 null, increment도 0이라 안 씀)
        LocalDateTime from = member.getLastResumedAt();
        LocalDateTime now = LocalDateTime.now();

        // RESTING 상태였다면(이미 pause 시점에 누적 끝남) increment=0이 된다.
        int increment = 0;
        if (member.getTimerStatus() == GroupRoomMember.TimerStatus.STUDYING) {
            increment = accumulateElapsed(member, now);
        }
        member.endTimer();
        GroupRoomMember saved = groupRoomMemberRepository.save(member);

        // solo와 동일한 이유로 방어적 처리
        // PAUSED 상태에서 end가 호출되면 이미 pause 시점에 랩이 마감되어 있어 closeLap()이 null을 반환 가능
        StudyLap closedLap = studyLapService.closeLap(roomId, saved.getId(), now);
        int lapNumber = (int) studyLapService.countLaps(roomId, saved.getId());

        // 증분 값이 존재할 때만 이벤트 발행
        // publishAccumulatedEvents 내부에 이미 increment<=0 가드 있어서 바깥 if 불필요
        publishAccumulatedEvents(userId, from, now, increment);
        eventPublisher.publishEvent(new TimerStatusChangedEvent(roomId, userId, null));

        log.info("[Study] 그룹 타이머 종료 | roomId={}, userId={}, increment={}", roomId, userId, increment);
        return TimerActionResult.ofEnded(saved, closedLap == null ? null : toLapItem(closedLap, lapNumber));
    }

    // 스케줄러 전용 - 조회 시점의 멤버 row id와 실제 처리 시점의 id가 일치할 때만 일시정지
    @Override
    public TimerActionResult pauseIfMatches(Long userId, Long roomId, Long expectedMemberId) {
        GroupRoomMember member = getJoinedMember(userId, roomId);
        if (!member.getId().equals(expectedMemberId)) {
            log.info("[Study] 멤버 상태가 이미 변경되어 자동 만료 스킵 | userId={}, roomId={}, expected={}, actual={}",
                    userId, roomId, expectedMemberId, member.getId());
            return null;
        }
        return pause(userId, roomId);
    }

    // ===== 내부 헬퍼 (MemberCommandService와 동일한 로직, 의도적으로 중복 유지) =====

    // 방 참가자(JOINED) 조회
    private GroupRoomMember getJoinedMember(Long userId, Long roomId) {
        return groupRoomMemberRepository.findByGroupRoomIdAndUserId(roomId, userId)
                .filter(GroupRoomMember::isJoined)
                .orElseThrow(() -> new StudyAccessDeniedException("그룹방 참가자만 가능한 동작입니다."));
    }

    // 유저가 다른 방/솔로에서 이미 타이머를 진행 중인지 검증 (동시 활성화 금지 정책)
    private void validateNoOtherActiveTimer(Long userId) {
        var studyingElsewhere = groupRoomMemberRepository.findAllByUserIdAndStudying(userId);
        if (!studyingElsewhere.isEmpty()) {
            throw new DomainRuleViolationException("이미 다른 곳에서 진행 중인 타이머가 있습니다.");
        }

        // 그룹방뿐 아니라 솔로 세션(RUNNING)도 진행 중이면 동시 활성화로 간주해 차단
        soloSessionRepository.findActiveByUserId(userId)
                .filter(session -> session.getStatus() == SoloSession.SoloSessionStatus.RUNNING)
                .ifPresent(session -> {
                    throw new DomainRuleViolationException("이미 다른 곳에서 진행 중인 타이머가 있습니다.");
                });
    }

    // lastResumedAt ~ now 구간 경과 시간을 계산해서 누적, 이에 더해진 증분을 반환
    private int accumulateElapsed(GroupRoomMember member, LocalDateTime now) {
        if (member.getLastResumedAt() == null) {
            return 0;
        }
        long elapsed = Duration.between(member.getLastResumedAt(), now).getSeconds();
        int increment = (int) Math.max(elapsed, 0);
        member.accumulateSeconds(increment);
        return increment;
    }

    // 자정 분할해서 이벤트 발행 (increment 파라미터는 검증/로그용, 실제 값은 from~to로 재계산)
    // StudySessionAccumulatedEvent(userId, studyDate, seconds) 하나였던 걸
    // MidnightSplitter로 쪼갠 개수만큼 여러 번 발행하도록 교체
    private void publishAccumulatedEvents(Long userId, LocalDateTime from, LocalDateTime to, int increment) {
        if (increment <= 0 || from == null) {
            return;
        }
        for (MidnightSplitter.DateSeconds part : MidnightSplitter.split(from, to)) {
            if (part.seconds() > 0) {
                eventPublisher.publishEvent(
                        new StudySessionAccumulatedEvent(userId, part.date(), part.seconds())
                );
            }
        }
    }

    // StudyLap(도메인) -> LapItem(Response) 변환 + 순번 매기기
    private LapItem toLapItem(StudyLap lap, int lapNumber) {
        return new LapItem(lapNumber, lap.getStartedAt(), lap.getEndedAt(), lap.getSeconds());
    }

}