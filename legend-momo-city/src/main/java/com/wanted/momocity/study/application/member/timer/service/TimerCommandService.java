package com.wanted.momocity.study.application.member.timer.service;

import com.wanted.momocity.study.application.member.timer.result.TimerActionResult;
import com.wanted.momocity.study.application.member.timer.usecase.TimerCommandUseCase;
import com.wanted.momocity.study.domain.event.StudySessionEndedEvent;
import com.wanted.momocity.study.domain.event.TimerStatusChangedEvent;
import com.wanted.momocity.study.domain.exception.StudyAccessDeniedException;
import com.wanted.momocity.study.domain.model.GroupRoomMember;
import com.wanted.momocity.study.domain.model.SoloSession;
import com.wanted.momocity.study.domain.repository.GroupRoomMemberRepository;
import com.wanted.momocity.study.domain.repository.SoloSessionRepository;
import com.wanted.momocity.global.domain.common.exception.DomainRuleViolationException;
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

    private final GroupRoomMemberRepository groupRoomMemberRepository;
    private final SoloSessionRepository soloSessionRepository;
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

        member.startTimer(LocalDateTime.now());
        GroupRoomMember saved = groupRoomMemberRepository.save(member);

        eventPublisher.publishEvent(new TimerStatusChangedEvent(roomId, userId, saved.getTimerStatus()));

        log.info("[Study] 그룹 타이머 시작 | roomId={}, userId={}, resumed={}", roomId, userId, wasResumed);
        return TimerActionResult.ofStarted(saved, wasResumed);
    }

    // 타이머 일시정지 - 누적 시간 확정 후 상태 전환
    @Override
    public TimerActionResult pause(Long userId, Long roomId) {

        GroupRoomMember member = getJoinedMember(userId, roomId);
        if (member.getTimerStatus() != GroupRoomMember.TimerStatus.STUDYING) {
            throw new DomainRuleViolationException("진행 중인 타이머가 없습니다.");
        }

        accumulateElapsed(member);
        member.pauseTimer();
        GroupRoomMember saved = groupRoomMemberRepository.save(member);

        eventPublisher.publishEvent(new TimerStatusChangedEvent(roomId, userId, saved.getTimerStatus()));

        log.info("[Study] 그룹 타이머 일시정지 | roomId={}, userId={}", roomId, userId);
        return TimerActionResult.ofPaused(saved);
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

        if (member.getTimerStatus() == GroupRoomMember.TimerStatus.STUDYING) {
            accumulateElapsed(member);
        }
        member.endTimer();
        GroupRoomMember saved = groupRoomMemberRepository.save(member);

        eventPublisher.publishEvent(
                new StudySessionEndedEvent(userId, LocalDateTime.now().toLocalDate(), saved.getTotalSeconds())
        );
        eventPublisher.publishEvent(new TimerStatusChangedEvent(roomId, userId, null));

        log.info("[Study] 그룹 타이머 종료 | roomId={}, userId={}", roomId, userId);
        return TimerActionResult.ofEnded(saved);
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

    // lastResumedAt ~ now 구간 경과 시간을 계산해서 누적
    private void accumulateElapsed(GroupRoomMember member) {
        if (member.getLastResumedAt() == null) {
            return;
        }
        long elapsed = Duration.between(member.getLastResumedAt(), LocalDateTime.now()).getSeconds();
        member.accumulateSeconds((int) Math.max(elapsed, 0));
    }
}