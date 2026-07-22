package com.wanted.momocity.study.infrastructure.persistence;

import com.wanted.momocity.study.domain.model.GroupRoomMember;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/*
 * comment.
 *  Spring Data JPA 가 구현체를 자동으로 생성
 *  -> Domain 을 모르고 JpaEntity 만 다룸
 * */

public interface GroupRoomMemberJpaRepository extends JpaRepository<GroupRoomMemberJpaEntity, Long> {

    // 특정 방 + 유저 조합 단건 조회 (uq_group_room_member 유니크 인덱스 활용)
    Optional<GroupRoomMemberJpaEntity> findByGroupRoomIdAndUserId(Long groupRoomId, Long userId);

    // 방의 현재 참가자 목록 (status=JOINED)
    List<GroupRoomMemberJpaEntity> findAllByGroupRoomIdAndStatus(
            Long groupRoomId, GroupRoomMember.MemberStatus status
    );

    // 유저가 특정 status로 걸쳐있는 멤버 row 전체 조회
    // ("내가 속한 그룹방 목록" = status JOINED, "내가 받은 초대 목록" = status INVITED 둘 다 이 메서드로 커버)
    List<GroupRoomMemberJpaEntity> findAllByUserIdAndStatus(
            Long userId, GroupRoomMember.MemberStatus status
    );

    /*
     * comment.
     *  유저가 현재 STUDYING 중인 멤버 row 조회 (방 무관, 전체)
     *  - 동시 타이머 검증(그룹방 타이머 시작 시, 이미 다른 방/솔로에서 진행 중인지 체크) 용도
     *  - status가 JOINED 이면서 timerStatus가 STUDYING인 row만 대상
     *  - Spring Data 파생 쿼리로 충분히 표현 가능하여 @Query 없이 메서드 이름으로 처리
     * */

    List<GroupRoomMemberJpaEntity> findAllByUserIdAndStatusAndTimerStatus(
            Long userId, GroupRoomMember.MemberStatus status, GroupRoomMember.TimerStatus timerStatus
    );

    /*
     * comment.
     *  24시간 초과로 방치된 STUDYING 멤버 목록 조회 (스케줄러용)
     *  - status=JOINED, timerStatus=STUDYING, timerStartedAt이 threshold보다 이전인 것
     *  - Spring Data 파생 쿼리로는 "이전"(Before) 조건이 자연스럽게 표현되므로 @Query 없이 처리 가능
     * */
    List<GroupRoomMemberJpaEntity> findAllByStatusAndTimerStatusAndTimerStartedAtBefore(
            GroupRoomMember.MemberStatus status,
            GroupRoomMember.TimerStatus timerStatus,
            LocalDateTime threshold
    );

}