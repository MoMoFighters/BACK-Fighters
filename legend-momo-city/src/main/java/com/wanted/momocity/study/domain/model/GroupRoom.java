package com.wanted.momocity.study.domain.model;

import java.time.LocalDateTime;

/*
 * comment.
 *  그룹 공부방(열품타 - 그룹) 도메인 역할 -> 순수 비즈니스 데이터만 담당 (JPA 모름)
 *  -
 *  방 자체의 생명주기만 담당 (생성 -> ACTIVE 유지 -> ENDED)
 *  방 안에서 일어나는 사람의 행위(초대/입퇴장/타이머)는 GroupRoomMember가 담당
 *  -
 *  소프트 딜리트
 *  - deletedAt == null : 정상(ACTIVE)
 *  - deletedAt != null : 종료된 방(ENDED), 추후 스케줄러가 하드딜리트
 *  -
 *  상태 검증(이미 종료된 방인지 등)은 Service(RoomCommandService)가 담당
 * */

public class GroupRoom {

    public static final int MAX_MEMBER = 4;

    private Long id;
    private Long hostUserId;
    private String inviteCode;
    private GroupRoomStatus status;
    private int maxMember;
    private LocalDateTime deletedAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // 신규 생성용
    public static GroupRoom create(Long hostUserId, String inviteCode) {
        GroupRoom room = new GroupRoom();
        room.hostUserId = hostUserId;
        room.inviteCode = inviteCode;
        room.status = GroupRoomStatus.ACTIVE;
        room.maxMember = MAX_MEMBER;
        return room;
    }

    // DB 복원용
    public static GroupRoom reconstitute(
            Long id, Long hostUserId, String inviteCode, GroupRoomStatus status,
            int maxMember, LocalDateTime deletedAt,
            LocalDateTime createdAt, LocalDateTime updatedAt
    ) {
        GroupRoom room = new GroupRoom();
        room.id = id;
        room.hostUserId = hostUserId;
        room.inviteCode = inviteCode;
        room.status = status;
        room.maxMember = maxMember;
        room.deletedAt = deletedAt;
        room.createdAt = createdAt;
        room.updatedAt = updatedAt;
        return room;
    }

    // 방장 위임 (방장이 나갔을 때 다음 입장자에게)
    public void changeHost(Long newHostUserId) {
        this.hostUserId = newHostUserId;
    }

    // 방 종료 (소프트딜리트, 인원 0명이 되었을 때)
    public void end(LocalDateTime now) {
        this.status = GroupRoomStatus.ENDED;
        this.deletedAt = now;
    }

    public boolean isHost(Long userId) {
        return this.hostUserId.equals(userId);
    }

    public boolean isActive() {
        return this.status == GroupRoomStatus.ACTIVE;
    }

    public Long getId() { return id; }
    public Long getHostUserId() { return hostUserId; }
    public String getInviteCode() { return inviteCode; }
    public GroupRoomStatus getStatus() { return status; }
    public int getMaxMember() { return maxMember; }
    public LocalDateTime getDeletedAt() { return deletedAt; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }

    public enum GroupRoomStatus {
        ACTIVE, ENDED
    }
}