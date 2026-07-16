package com.wanted.momocity.study.infrastructure.persistence;

import com.wanted.momocity.global.infrastructure.persistence.BaseTimeEntity;
import com.wanted.momocity.study.domain.model.GroupRoom;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/*
 * comment.
 *  DB 테이블(group_room)과 1:1 매핑되는 JPA 클래스
 *  -> Domain Model (GroupRoom) 을 모르고 DB 컬럼 구조만 표현
 *  -> 변환은 GroupRoomRepositoryAdapter 가 담당
 *  -
 *  소프트 딜리트
 *  - deletedAt == null : 정상(ACTIVE)
 *  - deletedAt != null : 종료된 방(ENDED)
 *  -
 *  group_room_member가 이 테이블을 FK(ON DELETE CASCADE)로 참조하지만, @OneToMany 연관관계는 맺지 않음
 *  (방 상세 조회 시 멤버 목록은 GroupRoomMemberRepository를 별도로 조회해서 조합 -
 *   room과 member는 서로 다른 애그리게이트 성격이 강해 fetch join으로 묶을 이유가 적음)
 * */

@Getter
@Entity
@Table(name = "group_room")
@NoArgsConstructor
public class GroupRoomJpaEntity extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "host_user_id", nullable = false)
    private Long hostUserId;

    @Column(name = "invite_code", nullable = false)
    private String inviteCode;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private GroupRoom.GroupRoomStatus status;

    @Column(name = "max_member", nullable = false)
    private byte maxMember;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    // Domain -> JpaEntity 변환 (저장용)
    public static GroupRoomJpaEntity from(GroupRoom domain) {
        GroupRoomJpaEntity entity = new GroupRoomJpaEntity();
        entity.id = domain.getId();
        entity.hostUserId = domain.getHostUserId();
        entity.inviteCode = domain.getInviteCode();
        entity.status = domain.getStatus();
        entity.maxMember = (byte)domain.getMaxMember();
        entity.deletedAt = domain.getDeletedAt();
        return entity;
    }

    // JpaEntity -> Domain 변환 (조회용)
    public GroupRoom toDomain() {
        return GroupRoom.reconstitute(
                id, hostUserId, inviteCode, status, maxMember, deletedAt,
                getCreatedAt(), getUpdatedAt()
        );
    }

}