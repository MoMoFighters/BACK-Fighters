package com.wanted.momocity.study.domain.repository;

import com.wanted.momocity.study.domain.model.GroupRoomMember;

import java.util.List;
import java.util.Optional;

/*
 * comment.
 *  GroupRoomMember 도메인 저장소 인터페이스
 *  - infrastructure 를 모르고 도메인 계층에서만 사용
 *  - 구현체 : GroupRoomMemberRepositoryAdapter
 *  -
 *  GroupRoomMember는 초대~참가~퇴장 전체 생명주기를 하나의 row로 관리하므로,
 *  같은 (roomId, userId) 조합이라도 시점에 따라 status만 다른 하나의 row를 계속 재사용
 *  (재초대 시 새 row를 만들지 않고 기존 row의 status를 갱신 - KICKED는 예외적으로 재초대 자체를 막음)
 * */

public interface GroupRoomMemberRepository {

    // 멤버 저장 (초대 생성, 상태 변경 전부 이 메서드로)
    GroupRoomMember save(GroupRoomMember member);

    // 멤버 단건 조회 (id 기준)
    Optional<GroupRoomMember> findById(Long memberId);

    // 특정 방 + 유저 조합으로 단건 조회 (uq_group_room_member 유니크 활용)
    // 초대 발송 시 "이미 초대/참가 이력이 있는지" 확인, 강퇴 이력(KICKED) 확인 등에 사용
    Optional<GroupRoomMember> findByGroupRoomIdAndUserId(Long groupRoomId, Long userId);

    // 방의 현재 참가자 목록 조회 (status=JOINED만)
    // 방 상세 조회, 랭킹 조회(동시에 DailyStudyRecord/MonthlyStudyRecord와 조합)에 사용
    List<GroupRoomMember> findAllByGroupRoomIdAndJoined(Long groupRoomId);

    // 방의 대기 중인 초대 목록 조회 (status=INVITED만)
    // 초대 발송 시 인원 선제 차단 계산(JOINED + INVITED 합산)에 사용
    List<GroupRoomMember> findAllByGroupRoomIdAndInvited(Long groupRoomId);

    // 유저가 현재 JOINED 상태로 속한 방 목록 조회 ("내가 속한 그룹방 목록" 용)
    List<GroupRoomMember> findAllByUserIdAndJoined(Long userId);

    // 유저가 받은 초대 목록 조회 (status=INVITED, room 무관 - roomId를 거치지 않는 독립 조회)
    List<GroupRoomMember> findAllByUserIdAndInvited(Long userId);

    // 특정 방에서 현재 타이머가 STUDYING인 멤버가 있는지 확인
    // (동시 타이머 검증 - 유저가 다른 방/솔로에서 이미 진행 중인지 체크할 때 활용)
    List<GroupRoomMember> findAllByUserIdAndStudying(Long userId);

}