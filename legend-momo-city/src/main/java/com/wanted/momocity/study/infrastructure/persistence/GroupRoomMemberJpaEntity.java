package com.wanted.momocity.study.infrastructure.persistence;

import com.wanted.momocity.global.infrastructure.persistence.BaseTimeEntity;
import com.wanted.momocity.study.domain.model.GroupRoomMember;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/*
 * comment.
 *  DB 테이블(group_room_member)과 1:1 매핑되는 JPA 클래스
 *  -> Domain Model (GroupRoomMember) 을 모르고 DB 컬럼 구조만 표현
 *  -> 변환은 GroupRoomMemberRepositoryAdapter 가 담당
 *  -
 *  uq_group_room_member (group_room_id, user_id) 유니크 제약이 DB에 걸려있으므로,
 *  같은 방-유저 조합은 항상 이 엔티티 하나만 존재 (재초대 시 status만 갱신).
 *  -
 *  group_room에 대한 FK(fk_group_room_member_room, ON DELETE CASCADE)가 걸려있지만,
 *  JPA 연관관계(@ManyToOne)는 맺지 않음
 *  -> Post/PostContent처럼 fetch join이 필요한 연관관계가 아니라 단순 FK 참조이므로,
 *     group_room_id를 Long 컬럼으로만 들고 필요 시 별도 조회 (불필요한 N+1/LAZY 로딩 방지)
 * */

@Getter
@Entity
@Table(name = "group_room_member")
@NoArgsConstructor
public class GroupRoomMemberJpaEntity extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "group_room_id", nullable = false)
    private Long groupRoomId;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private GroupRoomMember.MemberStatus status;

    @Enumerated(EnumType.STRING)
    @Column(name = "timer_status")
    private GroupRoomMember.TimerStatus timerStatus;

    /*
     * comment.
     *  마지막으로 STUDYING이 된 시각. 타이머 재개 시점 계산의 기준값.
     *  RESTING이거나 timerStatus=null이면 이 값도 null이어야 함
     *  서버 재시작/재조회 시에도 진행 중인 타이머의 경과시간을 정확히 복원하기 위해
     *  DB 컬럼(last_resumed_at)으로 영속화 (Redis는 별도로 실시간 상태 캐시 목적으로만 사용)
     * */

    @Column(name = "last_resumed_at")
    private LocalDateTime lastResumedAt;

    @Column(name = "total_seconds", nullable = false)
    private int totalSeconds;

    @Column(name = "invited_at")
    private LocalDateTime invitedAt;

    @Column(name = "joined_at")
    private LocalDateTime joinedAt;

    @Column(name = "left_at")
    private LocalDateTime leftAt;

    // Domain -> JpaEntity 변환 (저장용)
    public static GroupRoomMemberJpaEntity from(GroupRoomMember domain) {
        GroupRoomMemberJpaEntity entity = new GroupRoomMemberJpaEntity();
        entity.id = domain.getId();
        entity.groupRoomId = domain.getGroupRoomId();
        entity.userId = domain.getUserId();
        entity.status = domain.getStatus();
        entity.timerStatus = domain.getTimerStatus();
        entity.lastResumedAt = domain.getLastResumedAt();
        entity.totalSeconds = domain.getTotalSeconds();
        entity.invitedAt = domain.getInvitedAt();
        entity.joinedAt = domain.getJoinedAt();
        entity.leftAt = domain.getLeftAt();
        return entity;
    }

    // JpaEntity -> Domain 변환 (조회용)
    public GroupRoomMember toDomain() {
        return GroupRoomMember.reconstitute(
                id, groupRoomId, userId, status, timerStatus,
                lastResumedAt, totalSeconds,
                invitedAt, joinedAt, leftAt,
                getCreatedAt(), getUpdatedAt()
        );
    }

}