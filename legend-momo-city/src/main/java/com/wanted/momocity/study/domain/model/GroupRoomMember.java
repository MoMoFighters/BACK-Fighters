package com.wanted.momocity.study.domain.model;

import java.time.LocalDateTime;

/*
 * comment.
 *  그룹방 멤버 도메인 역할 -> 순수 비즈니스 데이터만 담당 (JPA 모름)
 *  -
 *  초대 - 참가 - 퇴장 전체 생명주기를 하나의 row(엔티티)로 통합 관리
 *  status 전이 (검증은 Service(MemberCommandService)가 담당, 여기서는 상태값만 변경) :
 *    INVITED -> JOINED   (초대 수락)
 *    INVITED -> REJECTED (초대받은 사람이 거절)
 *    INVITED -> CANCELED (초대한 사람이 취소)
 *    JOINED  -> LEFT     (자진 퇴장, 추후 재초대 가능)
 *    JOINED  -> KICKED   (방장이 강퇴, 이후 재초대 불가 - 검증은 Service 담당)
 *  -
 *  timerStatus는 JOINED 상태에서만 의미 (STUDYING / RESTING / null=타이머 종료)
 *  totalSeconds는 이 유저가 이 방에서 지금까지 누적한 공부 시간(초)
 * */

public class GroupRoomMember {

    private Long id;
    private Long groupRoomId;
    private Long userId;
    private MemberStatus status;
    private TimerStatus timerStatus;
    private LocalDateTime lastResumedAt;
    private int totalSeconds;
    private LocalDateTime timerStartedAt;
    private LocalDateTime invitedAt;
    private LocalDateTime joinedAt;
    private LocalDateTime leftAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // 신규 생성용 (초대)
    public static GroupRoomMember invite(Long groupRoomId, Long userId, LocalDateTime now) {
        GroupRoomMember member = new GroupRoomMember();
        member.groupRoomId = groupRoomId;
        member.userId = userId;
        member.status = MemberStatus.INVITED;
        member.invitedAt = now;
        member.totalSeconds = 0;
        return member;
    }

    // 신규 생성용 (방장은 생성과 동시에 JOINED)
    public static GroupRoomMember joinAsHost(Long groupRoomId, Long userId, LocalDateTime now) {
        GroupRoomMember member = new GroupRoomMember();
        member.groupRoomId = groupRoomId;
        member.userId = userId;
        member.status = MemberStatus.JOINED;
        member.invitedAt = now;
        member.joinedAt = now;
        member.totalSeconds = 0;
        return member;
    }

    // DB 복원용
    public static GroupRoomMember reconstitute(
            Long id, Long groupRoomId, Long userId, MemberStatus status, TimerStatus timerStatus,
            LocalDateTime lastResumedAt, int totalSeconds, LocalDateTime timerStartedAt,
            LocalDateTime invitedAt, LocalDateTime joinedAt, LocalDateTime leftAt,
            LocalDateTime createdAt, LocalDateTime updatedAt
    ) {
        GroupRoomMember member = new GroupRoomMember();
        member.id = id;
        member.groupRoomId = groupRoomId;
        member.userId = userId;
        member.status = status;
        member.timerStatus = timerStatus;
        member.lastResumedAt = lastResumedAt;
        member.totalSeconds = totalSeconds;
        member.timerStartedAt = timerStartedAt;
        member.invitedAt = invitedAt;
        member.joinedAt = joinedAt;
        member.leftAt = leftAt;
        member.createdAt = createdAt;
        member.updatedAt = updatedAt;
        return member;
    }

    // 초대 수락 (INVITED -> JOINED)
    public void accept(LocalDateTime now) {
        this.status = MemberStatus.JOINED;
        this.joinedAt = now;
    }

    // 초대 거절 (INVITED -> REJECTED)
    public void reject() {
        this.status = MemberStatus.REJECTED;
    }

    // 초대 취소 (INVITED -> CANCELED, 초대한 사람이 취소)
    public void cancel() {
        this.status = MemberStatus.CANCELED;
    }

    // 자진 퇴장 (JOINED -> LEFT)
    // 진행 중인 타이머 정리는 Service가 endTimer/accumulateSeconds 호출 후 이 메서드를 호출
    public void leave(LocalDateTime now) {
        this.status = MemberStatus.LEFT;
        this.timerStatus = null;
        this.lastResumedAt = null;
        this.timerStartedAt = null;
        this.leftAt = now;
    }

    // 재초대 (LEFT, REJECTED, CANCELED -> INVITED)
    // invite()로 새 인스턴스를 만들지 말고, 기존 row를 찾아 이 메서드로 상태만 되돌림
    public void reinvite(LocalDateTime now) {
        this.status = MemberStatus.INVITED;
        this.invitedAt = now;
        this.joinedAt = null;
        this.leftAt = null;
        this.totalSeconds = 0;
        this.timerStatus = null;
        this.lastResumedAt = null;
        this.timerStartedAt = null;
    }

    // 강퇴 (JOINED -> KICKED, 방장에 의한 처리)
    // 이후 재초대 가능 여부(KICKED는 불가) 검증은 초대 시점에 Service가 담당
    public void kick(LocalDateTime now) {
        this.status = MemberStatus.KICKED;
        this.timerStatus = null;
        this.lastResumedAt = null;
        this.timerStartedAt = null;
        this.leftAt = now;
    }

    // 타이머 시작 (처음 시작이든 재개든 동일 - action 구분은 Service/Result가 책임)
    public void startTimer(LocalDateTime now) {
        this.timerStatus = TimerStatus.STUDYING;
        this.lastResumedAt = now;
        if (this.timerStartedAt == null) {
            this.timerStartedAt = now;
        }
    }

    // 타이머 일시정지
    // 누적 시간 계산은 Service 담당 (accumulateSeconds 호출 후 이 메서드 호출)
    public void pauseTimer() {
        this.timerStatus = TimerStatus.RESTING;
        this.lastResumedAt = null;
    }

    // 타이머 완전 종료 (방은 유지, timerStatus만 null로 전환)
    public void endTimer() {
        this.timerStatus = null;
        this.lastResumedAt = null;
        this.timerStartedAt = null;
    }

    // 누적 시간 더하기 (Service가 lastResumedAt ~ now 구간을 계산해서 전달)
    public void accumulateSeconds(int seconds) {
        this.totalSeconds += Math.max(seconds, 0);
    }

    public boolean isJoined() {
        return this.status == MemberStatus.JOINED;
    }

    public boolean isInvited() {
        return this.status == MemberStatus.INVITED;
    }

    public boolean isStudying() {
        return this.timerStatus == TimerStatus.STUDYING;
    }

    public Long getId() { return id; }
    public Long getGroupRoomId() { return groupRoomId; }
    public Long getUserId() { return userId; }
    public MemberStatus getStatus() { return status; }
    public TimerStatus getTimerStatus() { return timerStatus; }
    public LocalDateTime getLastResumedAt() { return lastResumedAt; }
    public int getTotalSeconds() { return totalSeconds; }
    public LocalDateTime getTimerStartedAt() { return timerStartedAt; }
    public LocalDateTime getInvitedAt() { return invitedAt; }
    public LocalDateTime getJoinedAt() { return joinedAt; }
    public LocalDateTime getLeftAt() { return leftAt; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }

    public enum MemberStatus {
        INVITED, JOINED, LEFT, KICKED, REJECTED, CANCELED
    }

    public enum TimerStatus {
        STUDYING, RESTING
    }
}