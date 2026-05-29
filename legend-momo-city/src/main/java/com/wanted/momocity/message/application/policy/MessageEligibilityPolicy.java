package com.wanted.momocity.message.application.policy;

import com.wanted.momocity.friend.fmexception.FMBusinessRuleViolationException;
import com.wanted.momocity.friend.fmexception.FMResourceAccessDeniedException;
import com.wanted.momocity.friend.fmexception.FMResourceConflictException;
import com.wanted.momocity.friend.user.UserWithFMJpaEntity;
import com.wanted.momocity.global.domain.common.exception.DomainRuleViolationException;

import com.wanted.momocity.message.infrastructure.persistence.ChatRoomMemberJpaEntity;
import com.wanted.momocity.message.infrastructure.persistence.SpringDataChatRoomMemberRepository;

import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Slf4j
public class MessageEligibilityPolicy {

    private final SpringDataChatRoomMemberRepository springDataChatRoomMemberRepository;

    public MessageEligibilityPolicy(SpringDataChatRoomMemberRepository springDataChatRoomMemberRepository) {
        this.springDataChatRoomMemberRepository = springDataChatRoomMemberRepository;
    }

    //유저 상태, 친구 상태, 역할을 종합하여 (알 수 없음) 가공 여부를 판별하는 규칙
    public boolean determineNotActive(UserWithFMJpaEntity targetUser, String friendStatus, Long loginUserId) {
        //나와의 채팅인 경우 무조건 ACTIVE
        if (targetUser.getId().equals(loginUserId)) {
            return false;
        }

        //상대방이 강사인 경우: user 테이블의 ACTIVE 확인
        if ("TEACHER".equals(targetUser.getRole())) {
            return !"ACTIVE".equals(targetUser.getStatus());
        }

        //상대방이 학생인 경우: user 테이블의 ACTIVE 확인, friend 테이블의 status(BLOCK, none) 확인
        return !"ACTIVE".equals(targetUser.getStatus())
                || "BLOCK".equals(friendStatus)
                || "none".equals(friendStatus);
    }


    //채팅방 개설 자격 겸증(409)
    public void validateCreate(Long loginUserId, Long targetUserId, String friendStatus) {
        //나와의 채팅 개설 시도 차단
        if (loginUserId.equals(targetUserId)) {
            log.warn("[MessageEligibilityPolicy] 대화창 개설 실패 - 자신과 대화창 개설 시도함");
            throw new FMResourceConflictException("자기 자신과는 대화창을 개설할 수 없습니다.");
        }

        //무조건 FRIEND 상태여야 개설 가능
        if (!"FRIEND".equals(friendStatus)) {
            log.warn("[MessageEligibilityPolicy] 대화창 개설 실패 - 친구 상태가 아님 (현재 상태: {})", friendStatus);
            throw new FMResourceConflictException("대화창을 개설할 수 없는 사용자입니다.");
        }
    }

    //회원가입 완료 후 이벤트로 나와의 채팅방 개설 검증
    public boolean isSelfChatRoomExists(Long userId) {
        log.info("[MessageEligibilityPolicy] 회원가입 직후 나와의 채팅방 중복 생성 여부 검증 - 유저ID: {}", userId);

        //내가 참여 중인 행이 단 1개라도 있으면 이미 방이 파진 것
        return springDataChatRoomMemberRepository.existsByUserId_Id(userId);
    }

    //메시지 전송 검증
    public void sendable(Long roomId, Long senderId, String friendStatus, long roomMemberCount) {

        //로그인한 유저가 채팅방의 멤버가 맞는지 검증
        boolean isMember = springDataChatRoomMemberRepository.existsByRoomId_IdAndUserId_Id(roomId, senderId);

        if (!isMember) {
            log.warn("[MessageEligibilityPolicy] 권한 없음 - 요청 유저가 방의 멤버가 아님. 유저: {}, 방: {}", senderId, roomId);
            throw new FMResourceAccessDeniedException("해당 채팅방에 접근할 권한이 없습니다.");
        }

        //나와의 채팅방 여부 확인
        Long selfRoomId = springDataChatRoomMemberRepository.findFirstRoomIdByUserId_Id(senderId)
                .map(member -> member.getRoomId().getId())
                .orElse(null);
        //나와의 채팅방이라면 상대방 퇴장 및 친구 상태 검증 모두 통과
        if (roomId.equals(selfRoomId)) {
            log.info("[MessageEligibilityPolicy] 나와의 채팅방 메시지 전송 - 검증 패스. 방ID: {}", roomId);
            return;
        }
        //방에 혼자 남았다면 상대방이 나간 것(409)
        if (roomMemberCount < 2) {
            log.warn("[MessageEligibilityPolicy] 메시지 전송 실패 - 상대방이 방을 나갔음. 방ID: {}", roomId);
            throw new FMResourceConflictException("상대방이 채팅방을 나갔습니다.");
        }

        //친구 상태가 아니라면 전송 불가(409)
        if (!"FRIEND".equals(friendStatus)) {
            log.warn("[MessageEligibilityPolicy] 메시지 전송 실패 - 차단된 관계. 요청자: {}", senderId);
            throw new FMResourceConflictException("메시지를 전송할 수 없는 사용자입니다.");
        }

    }

    //채팅방 나가기 검증
    public void leaveChatRoom(Long userId, Long roomId, List<ChatRoomMemberJpaEntity> allMembers) {
        //로그인 유저가 해당 방의 멤버가 맞는지 확인(403)
        ChatRoomMemberJpaEntity myMembership = allMembers.stream()
                .filter(m -> m.getUserId().getId().equals(userId))
                .findFirst()
                .orElseThrow(() -> new FMResourceAccessDeniedException("해당 채팅방을 나갈 권한이 없습니다."));

        //사용자의 최초 방ID를 비교(나와의 채팅방)(409)
        List<ChatRoomMemberJpaEntity> myAllRooms = springDataChatRoomMemberRepository.findByUserId_Id(userId);
        Long firstRoomId = myAllRooms.stream()
                .map(member -> member.getRoomId().getId())
                .min(Long::compare)
                .orElse(-1L);

        if (roomId.equals(firstRoomId)) {
            log.warn("[MessageEligibilityPolicy] 나와의 채팅방은 퇴장할 수 없습니다. 유저: {}, 방: {}", userId, roomId);
            throw new FMResourceConflictException("나와의 채팅방은 나갈 수 없습니다.");
        }

        //강사가 포함된 채팅방인지 확인(강사는 친구 기능이 없고 학생이 개설해야만 존재)
        boolean hasTeacherInRoom = allMembers.stream()
                .anyMatch(m -> "TEACHER".equals(m.getUserId().getRole()));

        if (hasTeacherInRoom) {
            log.warn("[MessageEligibilityPolicy] 강사가 포함된 대화창은 퇴장할 수 없습니다. 요청 유저: {}, 방ID: {}", userId, roomId);
            throw new FMBusinessRuleViolationException("해당 채팅방을 나갈 권한이 없습니다. (강사와의 채팅방은 퇴장 불가)");
        }
    }
}
