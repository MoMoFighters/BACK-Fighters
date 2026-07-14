package com.wanted.momocity.study.application.member.service;

import com.wanted.momocity.study.application.member.command.InviteMemberCommand;
import com.wanted.momocity.study.application.member.port.FriendCatalogPort;
import com.wanted.momocity.study.application.member.result.InvitationResult;
import com.wanted.momocity.study.application.member.result.KickResult;
import com.wanted.momocity.study.application.member.result.LeaveResult;
import com.wanted.momocity.study.application.member.usecase.MemberCommandUseCase;
import com.wanted.momocity.study.domain.event.*;
import com.wanted.momocity.study.domain.exception.StudyAccessDeniedException;
import com.wanted.momocity.study.domain.exception.StudyNotFoundException;
import com.wanted.momocity.study.domain.model.GroupRoom;
import com.wanted.momocity.study.domain.model.GroupRoomMember;
import com.wanted.momocity.study.domain.repository.GroupRoomMemberRepository;
import com.wanted.momocity.study.domain.repository.GroupRoomRepository;
import com.wanted.momocity.study.infrastructure.redis.GroupRoomMemberCountAdapter;
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
 *  - 초대 발송/취소/수락/거절, 방 나가기, 강퇴
 *  -
 *  타이머 시작/일시정지/종료는 TimerCommandService(application.member.timer)로 이관
 *   "멤버 자격의 생명주기"와 "반복되는 타이머 상태 변화"는 다른 축의 개념이라 판단
 *  -
 *  검증(친구 여부, 방 상태, 인원 제한, 상태 전이 가능 여부 등)은 전부 이 Service가 담당
 *  domain.model(GroupRoomMember/GroupRoom)은 상태값만 바꾸는 순수 메서드만 제공
 *  -
 *  인원 제한(4명) 이중 체크
 *  1) 발송 시점(선제 차단) : DB에서 JOINED+INVITED 합산 개수로 대략 확인 (invite() 참고)
 *  2) 수락 시점(최종 방어선) : Redis 원자적 카운트(GroupRoomMemberCountAdapter.tryIncrement)로 확정
 * */

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class MemberCommandService implements MemberCommandUseCase {

    private final GroupRoomMemberRepository groupRoomMemberRepository;
    private final GroupRoomRepository groupRoomRepository;
    private final FriendCatalogPort friendCatalogPort;
    // Redis 원자적 인원 카운트 어댑터 - 수락 시점 최종 방어선
    private final GroupRoomMemberCountAdapter groupRoomMemberCountAdapter;
    private final ApplicationEventPublisher eventPublisher;

    /*
     * comment.
     *  친구 초대 발송
     *  -
     *  검증 순서 (앞 단계에서 걸리면 뒤 단계는 실행되지 않음) :
     *  1. 방이 살아있는지(ACTIVE) 확인
     *  2. 친구 관계 재검증 - 프론트가 이미 필터링했더라도 서버가 반드시 다시 확인
     *  3. 이 유저와의 기존 관계 이력 확인 - KICKED면 재초대 자체를 막고, 이미 INVITED/JOINED면 중복 초대를 막음
     *     LEFT/REJECTED/CANCELED는 재초대 허용
     *  4. 인원 선제 차단 - 최종 확정은 아니지만 방이 명백히 꽉 찬 경우 미리 걸러서 불필요한 초대가 쌓이는 것을 방지
     *     (실제 최종 확정은 수락 시점 Redis에서)
     * */

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

        // 알림 발송은 별도 이벤트(예: InvitationCreatedEvent)로 확장 가능 - 여기서는 notification 테이블 연동은 생략 -> 추후 구현
        log.info("[Study] 그룹방 초대 발송 완료 | roomId={}, inviterId={}, inviteeId={}", roomId, userId, inviteeId);
        return InvitationResult.ofInvited(saved);
    }

    // 초대 취소 (초대한 사람이)
    @Override
    public InvitationResult cancelInvitation(Long userId, Long roomId, Long invitationId) {

        GroupRoomMember member = getMemberById(invitationId);
        validateSameRoom(member, roomId);

        // 현재 정책상 초대 발송 자체 방장만 가능 -> 방장만 취소 가능하도록 방어
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

        /*
         * 기존에는 DB에서 findAllByGroupRoomIdAndJoined().size()로 카운트를 세서 비교하는 방식
         * 해당 방식은 동시에 여러 명이 수락할 때 "조회 -> 판단 -> 저장" 사이에 레이스 컨디션이 생길 수 있는 문제 존재
         * -> Redis INCR 기반 원자적 카운트(GroupRoomMemberCountAdapter.tryIncrement)로 교체
         * tryIncrement가 false를 반환하면 이미 정원이 가득 찬 것이므로 즉시 예외를 던지고,
         * 이 시점에는 아직 DB에 JOINED로 반영되지 않았으므로 Redis 카운트도 자동으로 롤백된 상태
         * */

        boolean acquired = groupRoomMemberCountAdapter.tryIncrement(roomId, room.getMaxMember());
        if (!acquired) {
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

    /*
     * comment.
     *  방 나가기 (자진 퇴장)
     *  -
     *  1. 타이머가 진행 중 -> 먼저 시간을 확정하고 StudySessionEndedEvent 발행
     *     (나가는 순간 공부 기록이 유실되지 않도록 leave보다 먼저 처리)
     *  2. member.leave() 저장 후 남은 인원(remaining)을 다시 조회
     *  3. remaining이 비어있으면 방 자체를 종료 (가장 우선순위 높은 케이스)
     *  4. 비어있지 않은데 나간 사람이 방장이었으면 최초 입장자(joinedAt 가장 이른 사람)에게 위임
     *  5. 둘 다 아니면 그냥 일반 퇴장
     *  -
     *  Redis 카운트(decrement)는 remaining 조회보다 먼저 호출
     *  - DB save와 Redis 반영 사이의 간격을 최소화하기 위함이며, 순서 자체가 결과에 영향을 주지는 않음
     * */

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

        // 인원 감소 -> Redis 카운트도 -1 (수락 시 tryIncrement로 +1 했던 것과 짝을 맞춤)
        groupRoomMemberCountAdapter.decrement(roomId);

        var remaining = groupRoomMemberRepository.findAllByGroupRoomIdAndJoined(roomId);

        // 마지막 사람이 나간 경우 -> 방 종료
        if (remaining.isEmpty()) {
            room.end(LocalDateTime.now());
            groupRoomRepository.save(room);
            // 방이 완전히 종료됐으므로 Redis 카운트 키 자체를 삭제 (decrement로 0 남기지 않고 clear)
            groupRoomMemberCountAdapter.clear(roomId);
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

    /*
     * comment.
     *  강퇴 (방장만 가능)
     *  -
     *  - target.kick()은 상태를 LEFT가 아닌 KICKED (재초대 불가 이력으로 남김)
     *  - 대상이 강퇴당하는 시점에 타이머가 STUDYING이었다면 leave()와 동일하게 먼저 시간을 확정
     * */

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

        // 강퇴로 인원 감소 -> Redis 카운트도 -1
        groupRoomMemberCountAdapter.decrement(roomId);

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
        return groupRoomMemberRepository.findAllByGroupRoomIdAndInvited(roomId).size();
    }

    // lastResumedAt ~ now 구간 경과 시간을 계산해서 누적 (leave/kick에서 타이머 종료 처리 시 사용)
    private void accumulateElapsed(GroupRoomMember member) {
        if (member.getLastResumedAt() == null) {
            return;
        }
        long elapsed = Duration.between(member.getLastResumedAt(), LocalDateTime.now()).getSeconds();
        member.accumulateSeconds((int) Math.max(elapsed, 0));
    }
}