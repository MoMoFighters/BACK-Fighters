package com.wanted.momocity.study.application.room.service;

import com.wanted.momocity.auth.domain.model.User;
import com.wanted.momocity.global.domain.common.exception.DomainRuleViolationException;
import com.wanted.momocity.study.application.common.port.StudyUserInfoPort;
import com.wanted.momocity.study.application.room.result.RoomCreateResult;
import com.wanted.momocity.study.application.room.result.RoomUpdateResult;
import com.wanted.momocity.study.application.room.usecase.RoomCommandUseCase;
import com.wanted.momocity.study.domain.exception.StudyAccessDeniedException;
import com.wanted.momocity.study.domain.exception.StudyNotFoundException;
import com.wanted.momocity.study.domain.model.GroupRoom;
import com.wanted.momocity.study.domain.model.GroupRoomMember;
import com.wanted.momocity.study.domain.repository.GroupRoomMemberRepository;
import com.wanted.momocity.study.domain.repository.GroupRoomRepository;
import com.wanted.momocity.study.infrastructure.redis.GroupRoomMemberCountAdapter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/*
 * comment.
 *  그룹방 자체 쓰기 작업 UseCase 구현체
 *  - 방 생성만 담당 (방장 위임/방 종료는 member 도메인에서 처리 - RoomCommandUseCase 주석 참고)
 *  -
 *  1. GroupRoom 저장
 *  2. GroupRoomMember를 JOINED 상태로 즉시 저장 (방장 본인)
 *     - GroupRoomMember.invite()가 아니라 joinAsHost()를 쓰는 이유는,
 *       방장은 초대 절차 없이 바로 참가 확정 상태여야 하기 때문 (INVITED를 거치지 않음)
 *  3. Redis 인원 카운트를 1로 초기화 (member 도메인의 GroupRoomMemberCountAdapter를 그대로 재사용 -
 *     이 어댑터는 room/member 어느 한쪽 소유가 아니라 study 도메인 공용 인프라라고 판단)
 * */

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class RoomCommandService implements RoomCommandUseCase {

    private final GroupRoomRepository groupRoomRepository;
    private final GroupRoomMemberRepository groupRoomMemberRepository;
    private final GroupRoomMemberCountAdapter groupRoomMemberCountAdapter;
    private final StudyUserInfoPort studyUserInfoPort;

    @Override
    public RoomCreateResult createRoom(Long userId, String title) {

        // 화면 표시용 닉네임 조회를 먼저 수행 (코드리뷰 반영 - 방/멤버 저장 전에 유저 존재 확인)
        User host = studyUserInfoPort.findById(userId)
                .orElseThrow(() -> new StudyNotFoundException("사용자를 찾을 수 없습니다."));

        // 방 생성
        GroupRoom room = GroupRoom.create(userId, title);
        GroupRoom savedRoom = groupRoomRepository.save(room);

        // 방장을 JOINED 상태로 즉시 참가시킴 (초대 절차 없이 바로 확정)
        GroupRoomMember hostMember = GroupRoomMember.joinAsHost(savedRoom.getId(), userId, LocalDateTime.now());
        groupRoomMemberRepository.save(hostMember);

        // Redis 인원 카운트 초기화 (방장 1명 포함)
        groupRoomMemberCountAdapter.initialize(savedRoom.getId(), 1);

        log.info("[Study] 그룹방 생성 완료 | roomId={}, hostUserId={}",
                savedRoom.getId(), userId);

        return new RoomCreateResult(
                savedRoom.getId(), savedRoom.getHostUserId(), savedRoom.getTitle(),
                host.getNickname(), savedRoom.getStatus().name(), savedRoom.getMaxMember()
        );
    }

    // 방 제목 수정 (방장만 가능)
    @Override
    public RoomUpdateResult updateTitle(Long userId, Long roomId, String newTitle) {
        GroupRoom room = groupRoomRepository.findByIdAndActive(roomId)
                .orElseThrow(() -> new StudyNotFoundException("그룹방을 찾을 수 없습니다."));

        if (!room.isHost(userId)) {
            throw new StudyAccessDeniedException("방장만 방 제목을 수정할 수 있습니다.");
        }

        room.updateTitle(newTitle);
        GroupRoom saved = groupRoomRepository.save(room);

        log.info("[Study] 그룹방 제목 수정 완료 | roomId={}, newTitle={}", roomId, newTitle);
        return new RoomUpdateResult(saved.getId(), saved.getTitle());
    }
}