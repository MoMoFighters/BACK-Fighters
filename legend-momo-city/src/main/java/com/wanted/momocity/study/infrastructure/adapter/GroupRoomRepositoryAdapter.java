package com.wanted.momocity.study.infrastructure.adapter;

import com.wanted.momocity.study.domain.model.GroupRoom;
import com.wanted.momocity.study.domain.repository.GroupRoomRepository;
import com.wanted.momocity.study.infrastructure.persistence.GroupRoomJpaEntity;
import com.wanted.momocity.study.infrastructure.persistence.GroupRoomJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

/*
 * comment.
 *  GroupRoomRepository 인터페이스 구현체
 *  -> domain.repository 인터페이스 <- 구현 -> JpaRepository 연결
 * */

@Component
@RequiredArgsConstructor
public class GroupRoomRepositoryAdapter implements GroupRoomRepository {

    private final GroupRoomJpaRepository groupRoomJpaRepository;

    // 방 저장 (생성, 수정)
    @Override
    public GroupRoom save(GroupRoom room) {
        return groupRoomJpaRepository.save(GroupRoomJpaEntity.from(room)).toDomain();
    }

    // 방 단건 조회 (status=ACTIVE인 것만)
    @Override
    public Optional<GroupRoom> findByIdAndActive(Long roomId) {
        return groupRoomJpaRepository.findByIdAndStatus(roomId, GroupRoom.GroupRoomStatus.ACTIVE)
                .map(GroupRoomJpaEntity::toDomain);
    }

    // 초대코드 중복 확인용 조회
    @Override
    public Optional<GroupRoom> findByInviteCode(String inviteCode) {
        return groupRoomJpaRepository.findByInviteCode(inviteCode)
                .map(GroupRoomJpaEntity::toDomain);
    }

    // 특정 유저가 host인 ACTIVE 방 목록 조회
    @Override
    public List<GroupRoom> findAllByHostUserIdAndActive(Long hostUserId) {
        return groupRoomJpaRepository
                .findAllByHostUserIdAndStatus(hostUserId, GroupRoom.GroupRoomStatus.ACTIVE)
                .stream()
                .map(GroupRoomJpaEntity::toDomain)
                .toList();
    }
}