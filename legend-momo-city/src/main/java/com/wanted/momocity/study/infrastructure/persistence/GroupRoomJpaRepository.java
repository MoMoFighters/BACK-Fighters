package com.wanted.momocity.study.infrastructure.persistence;

import com.wanted.momocity.study.domain.model.GroupRoom;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

/*
 * comment.
 *  Spring Data JPA 가 구현체를 자동으로 생성
 *  -> Domain 을 모르고 JpaEntity 만 다룸
 * */

public interface GroupRoomJpaRepository extends JpaRepository<GroupRoomJpaEntity, Long> {

    // 방 단건 조회 (소프트딜리트 제외, status=ACTIVE)
    Optional<GroupRoomJpaEntity> findByIdAndStatus(Long id, GroupRoom.GroupRoomStatus status);

    // 초대코드 중복 확인용 조회 (invite_code 생성 시 유니크 보장)
    Optional<GroupRoomJpaEntity> findByInviteCode(String inviteCode);

    // 특정 유저가 host인 ACTIVE 방 목록 조회
    List<GroupRoomJpaEntity> findAllByHostUserIdAndStatus(Long hostUserId, GroupRoom.GroupRoomStatus status);

}