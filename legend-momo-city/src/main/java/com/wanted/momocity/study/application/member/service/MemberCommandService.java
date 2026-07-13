package com.wanted.momocity.study.application.member.service;

import com.wanted.momocity.study.application.member.command.InviteMemberCommand;
import com.wanted.momocity.study.application.member.port.FriendCatalogPort;
import com.wanted.momocity.study.application.member.result.InvitationResult;
import com.wanted.momocity.study.application.member.result.KickResult;
import com.wanted.momocity.study.application.member.result.LeaveResult;
import com.wanted.momocity.study.application.member.result.TimerActionResult;
import com.wanted.momocity.study.application.member.usecase.MemberCommandUseCase;
import com.wanted.momocity.study.domain.event.*;
import com.wanted.momocity.study.domain.exception.StudyAccessDeniedException;
import com.wanted.momocity.study.domain.exception.StudyNotFoundException;
import com.wanted.momocity.study.domain.model.GroupRoom;
import com.wanted.momocity.study.domain.model.GroupRoomMember;
import com.wanted.momocity.study.domain.repository.GroupRoomMemberRepository;
import com.wanted.momocity.study.domain.repository.GroupRoomRepository;
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
 *  그룹방 멤버 쓰기 작업 UseCase 구현체
 *  - 초대 발송/취소/수락/거절, 타이머 시작/일시정지/종료, 방 나가기, 강퇴
 *  -
 *  검증(친구 여부, 방 상태, 인원 제한, 상태 전이 가능 여부 등)은 전부 이 Service가 담당
 *  domain.model(GroupRoomMember/GroupRoom)은 상태값만 바꾸는 순수 메서드만 제공
 *  -
 *  인원 제한(4명) 이중 체크 중 "발송 시점 선제 차단"은 여기서 JOINED+INVITED 합산으로 확인하고,
 *  "수락 시점 최종 방어선(Redis 원자적 카운트)"은 GroupRoomMemberCountAdapter가 별도로 담당
 *  (이 파일에서는 아직 Redis 어댑터를 주입하지 않았으므로, 수락 로직에 TODO로 표시해둠)
 * */

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class MemberCommandService implements MemberCommandUseCase {

    private final GroupRoomMemberRepository groupRoomMemberRepository;
    private final GroupRoomRepository groupRoomRepository;
    private final FriendCatalogPort friendCatalogPort;
    private final ApplicationEventPublisher eventPublisher;

    // 친구 초대 발송
    @Override
    public InvitationResult invite(Long userId, Long roomId, InviteMemberCommand command) {

        GroupRoom room = getActiveRoom(roomId);
        Long inviteeId = command.inviteeId();

        // 친구 관계 재검증 (프론트 필터링과 별개로 서버가 반드시 재검증)
        if (!friendCatalogPort.isFriend(userId, inviteeId)) {
            throw new DomainRuleViolationException("친구 관계가 아닌 사용자는 초대할 수 없습니다.");
        }

        // 기존 관계 이력 확인 (KICKED면 재초대 자체 차단, 그 외 상태면 재사용/에러 분기)
        var existing = groupRoomMemberRepository.findByGroupRoomIdAndUserId(roomId, inviteeId);
        if (existing.isPresent()) {
            GroupRoomMember member = existing.get();
            if (member.getStatus() == GroupRoomMember.MemberStatus.KICKED) {
                throw new DomainRuleViolationException("강퇴된 사용자는 다시 초대할 수 없습니다.");
            }
            if (member.getStatus() == GroupRoomMember.MemberStatus.INVITED
                    || member.getStatus() == GroupRoomMember.MemberStatus.JOINED) {
                throw new DomainRuleViolationException("이미 초대되었거나 참가 중인 사용자입니다.");
            }
            // LEFT/REJECTED/CANCELED 이력이 있으면 새로 초대 가능 -> 아래에서 새 row 생성
        }

        // 인원 제한 선제 차단 (현재 JOINED + 대기 중 INVITED 합산 기준)
        long currentCount = groupRoomMemberRepository.findAllByGroupRoomIdAndJoined(roomId).size()
                + countPendingInvitations(roomId);
        if (currentCount >= room.getMaxMember()) {
            throw new DomainRuleViolationException("그룹방 인원이 가득 찼습니다.");
        }

        GroupRoomMember invited = GroupRoomMember.invite(roomId, inviteeId, LocalDateTime.now());
        GroupRoomMember saved = groupRoomMemberRepository.save(invited);

        // 알림 발송은 별도 이벤트(예: InvitationCreatedEvent)로 확장 가능 - 여기서는 notification 테이블 연동은 생략
        log.info("[Study] 그룹방 초대 발송 완료 | roomId={}, inviterId={}, inviteeId={}", roomId, userId, inviteeId);
        return InvitationResult.ofInvited(saved);
    }

    // 초대 취소 (초대한 사람이)
    @Override
    public InvitationResult cancelInvitation(Long userId, Long roomId, Long invitationId) {

        GroupRoomMember member = getMemberById(invitationId);
        validateSameRoom(member, roomId);

        // 본인이 발송한 초대인지는 "방장 or 발송자 정책"에 따라 달라질 수 있으나,
        // 현재 정책상 초대 발송 자체를 누구나 할 수 있다고 가정하지 않았으므로
        // 방장만 취소 가능하도록 방어 - 추후 "누구나 초대 가능" 정책이면 발송자 필드 추가 필요
        GroupRoom room = getActiveRoom(roomId);
        if (!room.isHost(userId)) {
            throw new StudyAccessDeniedException("본인이 발송한 초대만 취소할 수 있습니다.");
        }
        if (!member.isInvited()) {
            throw new DomainRuleViolationException("이미 처리된 초대입니다.");
        }

        member.cancel();
        GroupRoomMember saved = groupRoomMemberRepository.save(member);

        log.info("[Study] 초대 취소 완료 | roomId={}, invitationId={}", roomId, invitationId);
        return InvitationResult.ofCanceled(saved);
    }

    // 초대 수락 (본인 토큰 기준)
    @Override
    public InvitationResult acceptInvitation(Long userId, Long roomId) {

        GroupRoom room = getActiveRoom(roomId);
        GroupRoomMember member = getMyInvitation(userId, roomId);

        // TODO: Redis 원자적 카운트(INCR) 최종 검증 - GroupRoomMemberCountAdapter 연동 후 대체
        long currentCount = groupRoomMemberRepository.findAllByGroupRoomIdAndJoined(roomId).size();
        if (currentCount >= room.getMaxMember()) {
            throw new DomainRuleViolationException("그룹방 인원이 가득 찼습니다.");
        }

        member.accept(LocalDateTime.now());
        GroupRoomMember saved = groupRoomMemberRepository.save(member);

        eventPublisher.publishEvent(new MemberJoinedEvent(roomId, userId));

        log.info("[Study] 초대 수락 완료 | roomId={}, userId={}", roomId, userId);
        return InvitationResult.ofAccepted(saved);
    }

    // 초대 거절 (본인 토큰 기준)
    @Override
    public InvitationResult rejectInvitation(Long userId, Long roomId) {

        GroupRoomMember member = getMyInvitation(userId, roomId);
        member.reject();
        GroupRoomMember saved = groupRoomMemberRepository.save(member);

        log.info("[Study] 초대 거절 완료 | roomId={}, userId={}", roomId, userId);
        return InvitationResult.ofRejected(saved);
    }

    // 타이머 시작 (신규 시작 + 재개 통합)
    @Override
    public TimerActionResult startTimer(Long userId, Long roomId) {

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

    // 타이머 일시정지
    @Override
    public TimerActionResult pauseTimer(Long userId, Long roomId) {

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

    // 타이머 완전 종료 (방은 유지)
    @Override
    public TimerActionResult endTimer(Long userId, Long roomId) {

        GroupRoomMember member = getJoinedMember(userId, roomId);
        if (member.getTimerStatus() == null) {
            throw new DomainRuleViolationException("진행 중인 타이머가 없습니다.");
        }

        if (member.getTimerStatus() == GroupRoomMember.TimerStatus.STUDYING) {
            accumulateElapsed(member);
        }
        member.endTimer();
        GroupRoomMember saved = groupRoomMemberRepository.save(member);

        // 자정 분할 로직은 StudySessionEndedEvent를 발행하는 공통 유틸(예: StudyEventPublishHelper)에서
        // 처리하도록 분리하는 게 이상적이나, 우선 단순화하여 하루치로 발행한다.
        eventPublisher.publishEvent(
                new StudySessionEndedEvent(userId, LocalDateTime.now().toLocalDate(), saved.getTotalSeconds())
        );
        eventPublisher.publishEvent(new TimerStatusChangedEvent(roomId, userId, null));

        log.info("[Study] 그룹 타이머 종료 | roomId={}, userId={}", roomId, userId);
        return TimerActionResult.ofEnded(saved);
    }

    // 방 나가기 (자진 퇴장)
    @Override
    public LeaveResult leave(Long userId, Long roomId) {

        GroupRoomMember member = getJoinedMember(userId, roomId);
        GroupRoom room = getActiveRoom(roomId);

        // 진행 중인 타이머가 있으면 먼저 종료 처리
        if (member.getTimerStatus() == GroupRoomMember.TimerStatus.STUDYING) {
            accumulateElapsed(member);
            eventPublisher.publishEvent(
                    new StudySessionEndedEvent(userId, LocalDateTime.now().toLocalDate(), member.getTotalSeconds())
            );
        }

        member.leave(LocalDateTime.now());
        groupRoomMemberRepository.save(member);

        var remaining = groupRoomMemberRepository.findAllByGroupRoomIdAndJoined(roomId);

        // 마지막 사람이 나간 경우 -> 방 종료
        if (remaining.isEmpty()) {
            room.end(LocalDateTime.now());
            groupRoomRepository.save(room);
            eventPublisher.publishEvent(new RoomEndedEvent(roomId));
            log.info("[Study] 마지막 인원 퇴장으로 방 종료 | roomId={}", roomId);
            return LeaveResult.of(roomId, false, null, true);
        }

        // 방장이 나간 경우 -> 최초 입장자에게 위임
        if (room.isHost(userId)) {
            GroupRoomMember nextHost = remaining.stream()
                    .min((a, b) -> a.getJoinedAt().compareTo(b.getJoinedAt()))
                    .orElseThrow(() -> new StudyNotFoundException("위임할 멤버를 찾을 수 없습니다."));
            room.changeHost(nextHost.getUserId());
            groupRoomRepository.save(room);
            eventPublisher.publishEvent(new HostChangedEvent(roomId, nextHost.getUserId()));
            eventPublisher.publishEvent(new MemberLeftEvent(roomId, userId));
            log.info("[Study] 방장 퇴장, 위임 완료 | roomId={}, newHostId={}", roomId, nextHost.getUserId());
            return LeaveResult.of(roomId, true, nextHost.getUserId(), false);
        }

        eventPublisher.publishEvent(new MemberLeftEvent(roomId, userId));
        log.info("[Study] 일반 멤버 퇴장 | roomId={}, userId={}", roomId, userId);
        return LeaveResult.of(roomId, false, null, false);
    }

    // 강퇴 (방장만 가능)
    @Override
    public KickResult kick(Long hostUserId, Long roomId, Long targetUserId) {

        GroupRoom room = getActiveRoom(roomId);
        if (!room.isHost(hostUserId)) {
            throw new StudyAccessDeniedException("방장만 멤버를 강퇴할 수 있습니다.");
        }
        if (hostUserId.equals(targetUserId)) {
            throw new DomainRuleViolationException("본인은 강퇴할 수 없습니다.");
        }

        GroupRoomMember target = groupRoomMemberRepository.findByGroupRoomIdAndUserId(roomId, targetUserId)
                .filter(GroupRoomMember::isJoined)
                .orElseThrow(() -> new StudyNotFoundException("그룹방 참가자가 아닙니다."));

        if (target.getTimerStatus() == GroupRoomMember.TimerStatus.STUDYING) {
            accumulateElapsed(target);
            eventPublisher.publishEvent(
                    new StudySessionEndedEvent(targetUserId, LocalDateTime.now().toLocalDate(), target.getTotalSeconds())
            );
        }

        target.kick(LocalDateTime.now());
        groupRoomMemberRepository.save(target);

        eventPublisher.publishEvent(new MemberKickedEvent(roomId, targetUserId, hostUserId));
        log.info("[Study] 멤버 강퇴 완료 | roomId={}, targetUserId={}, hostUserId={}", roomId, targetUserId, hostUserId);

        return KickResult.of(roomId, targetUserId);
    }

    // ===== 내부 헬퍼 =====

    private GroupRoom getActiveRoom(Long roomId) {
        return groupRoomRepository.findByIdAndActive(roomId)
                .orElseThrow(() -> new StudyNotFoundException("그룹방을 찾을 수 없습니다."));
    }

    private GroupRoomMember getMemberById(Long memberId) {
        return groupRoomMemberRepository.findById(memberId)
                .orElseThrow(() -> new StudyNotFoundException("초대 내역을 찾을 수 없습니다."));
    }

    private void validateSameRoom(GroupRoomMember member, Long roomId) {
        if (!member.getGroupRoomId().equals(roomId)) {
            throw new StudyNotFoundException("초대 내역을 찾을 수 없습니다.");
        }
    }

    // 본인이 받은 INVITED 상태의 멤버 row 조회 (수락/거절 공용)
    private GroupRoomMember getMyInvitation(Long userId, Long roomId) {
        GroupRoomMember member = groupRoomMemberRepository.findByGroupRoomIdAndUserId(roomId, userId)
                .orElseThrow(() -> new StudyNotFoundException("초대 내역을 찾을 수 없습니다."));
        if (!member.isInvited()) {
            throw new DomainRuleViolationException("이미 처리된 초대입니다.");
        }
        return member;
    }

    // 방 참가자(JOINED) 조회 - 타이머/퇴장 액션 공용
    private GroupRoomMember getJoinedMember(Long userId, Long roomId) {
        return groupRoomMemberRepository.findByGroupRoomIdAndUserId(roomId, userId)
                .filter(GroupRoomMember::isJoined)
                .orElseThrow(() -> new StudyAccessDeniedException("그룹방 참가자만 가능한 동작입니다."));
    }

    // 대기 중인 초대(INVITED) 개수 - 인원 선제 차단 계산용
    private long countPendingInvitations(Long roomId) {
        return groupRoomMemberRepository.findAllByGroupRoomIdAndJoined(roomId).stream()
                .filter(m -> m.getStatus() == GroupRoomMember.MemberStatus.INVITED)
                .count();
        // 주의: findAllByGroupRoomIdAndJoined는 이름상 JOINED만 조회하므로,
        // 실제로는 domain.repository에 findAllByGroupRoomIdAndStatus(INVITED) 같은 메서드를
        // 별도로 추가해서 교체해야 한다. 우선 로직 자리만 표시해둔다.
    }

    // 유저가 다른 방/솔로에서 이미 타이머를 진행 중인지 검증 (동시 활성화 금지 정책)
    private void validateNoOtherActiveTimer(Long userId) {
        var studyingElsewhere = groupRoomMemberRepository.findAllByUserIdAndStudying(userId);
        if (!studyingElsewhere.isEmpty()) {
            throw new DomainRuleViolationException("이미 다른 곳에서 진행 중인 타이머가 있습니다.");
        }
        // TODO: SoloSessionRepository.findActiveByUserId(userId)도 함께 확인해야
        // "그룹 타이머 켤 때 솔로 세션이 돌고 있는지"까지 완전히 검증된다. (SoloSessionRepository 주입 필요)
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