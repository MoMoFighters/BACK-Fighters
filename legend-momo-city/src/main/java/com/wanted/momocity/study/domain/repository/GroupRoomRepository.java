package com.wanted.momocity.study.domain.repository;

import com.wanted.momocity.study.domain.model.GroupRoom;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/*
 * comment.
 *  GroupRoom 도메인 저장소 인터페이스
 *  - infrastructure 를 모르고 도메인 계층에서만 사용
 *  - 구현체 : GroupRoomRepositoryAdapter
 *  -
 *  GroupRoom 자체는 "방"이라는 실체만 다루고, 멤버 목록/인원수는 다루지 않음
 *  (멤버 관련 조회는 GroupRoomMemberRepository 담당)
 * */

public interface GroupRoomRepository {

    // 방 저장 (생성, 수정)
    GroupRoom save(GroupRoom room);

    // 방 단건 조회 (소프트딜리트 제외, status=ACTIVE)
    Optional<GroupRoom> findByIdAndActive(Long roomId);

    // 특정 유저가 host인 ACTIVE 방 목록 조회
    // (member 목록 조회는 별도 - "내가 속한 그룹방 목록"은 GroupRoomMemberRepository와 조합해서 application에서 처리)
    List<GroupRoom> findAllByHostUserIdAndActive(Long hostUserId);

    // ENDED 상태로 특정 시각 이전에 종료된 방 목록 조회 (하드딜리트 대상 찾기용)
    List<GroupRoom> findAllByStatusAndDeletedAtBefore(GroupRoom.GroupRoomStatus status, LocalDateTime threshold);

    // 방 하드딜리트 (group_room_member는 FK CASCADE로 자동 삭제됨)
    void deleteById(Long roomId);

}