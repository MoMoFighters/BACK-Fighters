package com.wanted.momocity.study.infrastructure.adapter;

import com.wanted.momocity.study.domain.model.GroupRoomMember;
import com.wanted.momocity.study.domain.repository.GroupRoomMemberRepository;
import com.wanted.momocity.study.infrastructure.persistence.GroupRoomMemberJpaEntity;
import com.wanted.momocity.study.infrastructure.persistence.GroupRoomMemberJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

/*
 * comment.
 *  GroupRoomMemberRepository 인터페이스 구현체
 *  -> domain.repository 인터페이스 <- 구현 -> JpaRepository 연결
 *  - 저장 : Domain -> JpaEntity (from()) -> DB 저장
 *  - 조회 : DB 조회 -> JpaEntity -> Domain (toDomain())
 * */

@Component
@RequiredArgsConstructor
public class GroupRoomMemberRepositoryAdapter implements GroupRoomMemberRepository {

    private final GroupRoomMemberJpaRepository groupRoomMemberJpaRepository;

    // 멤버 저장 (초대 생성, 상태 변경 전부 이 메서드로)
    @Override
    public GroupRoomMember save(GroupRoomMember member) {
        return groupRoomMemberJpaRepository.save(GroupRoomMemberJpaEntity.from(member)).toDomain();
    }

    // 멤버 단건 조회 (id 기준)
    @Override
    public Optional<GroupRoomMember> findById(Long memberId) {
        return groupRoomMemberJpaRepository.findById(memberId)
                .map(GroupRoomMemberJpaEntity::toDomain);
    }

    // 특정 방 + 유저 조합으로 단건 조회
    @Override
    public Optional<GroupRoomMember> findByGroupRoomIdAndUserId(Long groupRoomId, Long userId) {
        return groupRoomMemberJpaRepository.findByGroupRoomIdAndUserId(groupRoomId, userId)
                .map(GroupRoomMemberJpaEntity::toDomain);
    }

    // 방의 현재 참가자 목록 조회 (status=JOINED만)
    @Override
    public List<GroupRoomMember> findAllByGroupRoomIdAndJoined(Long groupRoomId) {
        return groupRoomMemberJpaRepository
                .findAllByGroupRoomIdAndStatus(groupRoomId, GroupRoomMember.MemberStatus.JOINED)
                .stream()
                .map(GroupRoomMemberJpaEntity::toDomain)
                .toList();
    }

    // 유저가 현재 JOINED 상태로 속한 방 목록 조회
    @Override
    public List<GroupRoomMember> findAllByUserIdAndJoined(Long userId) {
        return groupRoomMemberJpaRepository
                .findAllByUserIdAndStatus(userId, GroupRoomMember.MemberStatus.JOINED)
                .stream()
                .map(GroupRoomMemberJpaEntity::toDomain)
                .toList();
    }

    // 유저가 받은 초대 목록 조회 (status=INVITED, room 무관)
    @Override
    public List<GroupRoomMember> findAllByUserIdAndInvited(Long userId) {
        return groupRoomMemberJpaRepository
                .findAllByUserIdAndStatus(userId, GroupRoomMember.MemberStatus.INVITED)
                .stream()
                .map(GroupRoomMemberJpaEntity::toDomain)
                .toList();
    }

    // 유저가 현재 STUDYING 중인 멤버 row 조회 (동시 타이머 검증용)
    @Override
    public List<GroupRoomMember> findAllByUserIdAndStudying(Long userId) {
        return groupRoomMemberJpaRepository
                .findAllByUserIdAndStatusAndTimerStatus(
                        userId, GroupRoomMember.MemberStatus.JOINED, GroupRoomMember.TimerStatus.STUDYING
                )
                .stream()
                .map(GroupRoomMemberJpaEntity::toDomain)
                .toList();
    }
}