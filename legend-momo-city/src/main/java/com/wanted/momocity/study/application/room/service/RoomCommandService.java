package com.wanted.momocity.study.application.room.service;

import com.wanted.momocity.auth.domain.model.User;
import com.wanted.momocity.study.application.common.port.StudyUserInfoPort;
import com.wanted.momocity.study.application.room.result.RoomCreateResult;
import com.wanted.momocity.study.application.room.usecase.RoomCommandUseCase;
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

import java.security.SecureRandom;
import java.time.LocalDateTime;

/*
 * comment.
 *  그룹방 자체 쓰기 작업 UseCase 구현체
 *  - 방 생성만 담당 (방장 위임/방 종료는 member 도메인에서 처리 - RoomCommandUseCase 주석 참고)
 *  -
 *  1. GroupRoom 저장 (invite_code는 여기서 랜덤 생성 후 유니크 재시도)
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

    // invite_code 문자 구성 - 혼동되기 쉬운 문자(0/O, 1/I) 제외
    private static final String INVITE_CODE_CHARS = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";
    private static final int INVITE_CODE_LENGTH = 6;
    private static final SecureRandom RANDOM = new SecureRandom();

    private final GroupRoomRepository groupRoomRepository;
    private final GroupRoomMemberRepository groupRoomMemberRepository;
    private final GroupRoomMemberCountAdapter groupRoomMemberCountAdapter;
    private final StudyUserInfoPort studyUserInfoPort;

    @Override
    public RoomCreateResult createRoom(Long userId) {

        // 방 생성 - 유니크한 초대코드가 나올 때까지 재시도
        String inviteCode = generateUniqueInviteCode();
        GroupRoom room = GroupRoom.create(userId, inviteCode);
        GroupRoom savedRoom = groupRoomRepository.save(room);

        // 방장을 JOINED 상태로 즉시 참가시킴 (초대 절차 없이 바로 확정)
        GroupRoomMember hostMember = GroupRoomMember.joinAsHost(savedRoom.getId(), userId, LocalDateTime.now());
        groupRoomMemberRepository.save(hostMember);

        // Redis 인원 카운트 초기화 (방장 1명 포함)
        groupRoomMemberCountAdapter.initialize(savedRoom.getId(), 1);

        // 화면 표시용 닉네임 조회 (본인 정보라 실패할 일이 거의 없지만, 방어적으로 예외 처리)
        User host = studyUserInfoPort.findById(userId)
                .orElseThrow(() -> new StudyNotFoundException("사용자를 찾을 수 없습니다."));

        log.info("[Study] 그룹방 생성 완료 | roomId={}, hostUserId={}, inviteCode={}",
                savedRoom.getId(), userId, inviteCode);

        return new RoomCreateResult(
                savedRoom.getId(), savedRoom.getHostUserId(), host.getName(),
                savedRoom.getInviteCode(), savedRoom.getStatus().name(), savedRoom.getMaxMember()
        );
    }

    /*
     * comment.
     *  중복되지 않는 초대코드를 생성
     *  - 6자리 랜덤 문자열(대문자+숫자, 혼동되는 문자 제외)을 만들고 DB에 존재하는지 확인, 존재시 재시도
     *    실무적으로 충돌이 거의 없지만, 최대 5회까지만 재시도 후 실패시 예외로 처리
     * */
    private String generateUniqueInviteCode() {
        for (int attempt = 0; attempt < 5; attempt++) {
            String candidate = generateRandomCode();
            if (groupRoomRepository.findByInviteCode(candidate).isEmpty()) {
                return candidate;
            }
        }
        throw new IllegalStateException("초대코드 생성에 반복적으로 실패했습니다. 잠시 후 다시 시도해주세요.");
    }

    private String generateRandomCode() {
        StringBuilder sb = new StringBuilder(INVITE_CODE_LENGTH);
        for (int i = 0; i < INVITE_CODE_LENGTH; i++) {
            sb.append(INVITE_CODE_CHARS.charAt(RANDOM.nextInt(INVITE_CODE_CHARS.length())));
        }
        return sb.toString();
    }
}