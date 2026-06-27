package com.wanted.momocity.message.application.policy;

import com.wanted.momocity.friend.fmexception.FMBusinessRuleViolationException;
import com.wanted.momocity.friend.fmexception.FMResourceAccessDeniedException;
import com.wanted.momocity.friend.fmexception.FMResourceConflictException;
import com.wanted.momocity.friend.infrastructure.persistence.FriendJpaEntity;
import com.wanted.momocity.friend.user.UserWithFMJpaEntity;
import com.wanted.momocity.global.domain.common.exception.DomainRuleViolationException;

import com.wanted.momocity.message.domain.repository.MessageRepository;
import com.wanted.momocity.message.infrastructure.persistence.ChatRoomJpaEntity;
import com.wanted.momocity.message.infrastructure.persistence.ChatRoomMemberJpaEntity;
import com.wanted.momocity.message.infrastructure.persistence.SpringDataChatRoomMemberRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Component
@Slf4j
@RequiredArgsConstructor
public class MessageEligibilityPolicy {

    private final MessageRepository messageRepository;


    //유저 상태, 친구 상태, 역할을 종합하여 (알 수 없음) 가공 여부를 판별하는 규칙
    public boolean determineNotActive(UserWithFMJpaEntity targetUser, String friendStatus, Long loginUserId) {
        //나와의 채팅인 경우 무조건 가공 대상 아님
        if (targetUser.getId().equals(loginUserId)) {
            return false;
        }

        //상대방이 강사인 경우: user 테이블의 ACTIVE 확인
        //v2 -> 강사인 경우 친구 관계 변동 없으므로 가공 대상 아님
        if ("TEACHER".equals(targetUser.getRole())) {
            return false;
        }

        //상대방이 학생인 경우: user 테이블의 ACTIVE 확인, friend 테이블의 status(BLOCK, none) 확인
        //v2 -> 비활성 여부에 대한 가공은 user담당자가 처리하므로 친구 여부만 검증
        return "BLOCK".equals(friendStatus) || "none".equals(friendStatus);
    }


    //채팅방 개설 자격 겸증(409)
    public void validateCreate(Long loginUserId, String roomTitle, List<UserWithFMJpaEntity> targetUsers, List<String> friendStatuses) {
        //빈 멤버 검증(아무도 선택 안하고 개설 시도)
        if (targetUsers == null || targetUsers.isEmpty()) {
            log.warn("[MessageEligibilityPolicy] 대화창 개설 시패 - 초대된 멤버가 없음");
            throw new FMBusinessRuleViolationException("채팅방 멤버는 최소 1명 이상 지정해야 합니다.");
        }

        //동일 멤버 중복 포함
        if (targetUsers.stream().map(UserWithFMJpaEntity::getId).distinct().count() != targetUsers.size()) {
            log.warn("[MessageEligibilityPolicy] 대화창 개설 실패 - 동일 사용자 중복 선택 개설");
            throw new FMResourceConflictException("동일한 사용자를 중복하여 개설할 수 없습니다.");
        }

        //개설할 인원이 1명인데 방 제목을 입력한 경우 차단: 일대일엔 방제목 없음
        if (targetUsers.size() == 1 && roomTitle != null && !roomTitle.trim().isEmpty()) {
            log.warn("[MessageEligibilityPolicy] 대화창 개설 실패 - 일대일 채팅방 개설 시 방 제목 지정 불가. 입력된 제목:{}", roomTitle);
            throw new FMBusinessRuleViolationException("일대일 채팅방은 방 제목을 지정할 수 없습니다.");
        }

        //일대일 구분: 방 이름 없는지 확인(없으면 일대일, 있으면 다대다)
        boolean isOneToOne = (roomTitle == null || roomTitle.isEmpty()) && targetUsers.size() == 1;

        //방이름 설정 공백 불가(다대다의 경우만 해당)
        if (!isOneToOne && (roomTitle == null || roomTitle.trim().isEmpty())) {
            log.warn("[MessageEligibilityPolicy] 대화창 개설 실패 - 방 제목이 공백으로만 이루어짐");
            throw new FMBusinessRuleViolationException("방 제목은 공백일 수 없습니다.");
        }

        //방 이름 20자 제한
        if (roomTitle != null && roomTitle.length() > 20) {
            log.warn("[MessageEligibilityPolicy] 대화창 개설 실패 - 방 제목 글자수 제한 초과. 입력된 글자수:{}", roomTitle.length());
            throw new FMBusinessRuleViolationException("방 제목은 최대 20자까지 가능합니다.");
        }

        //일대일, 다대다 모두 로그인 유저 포함 차단
        boolean hasMe = targetUsers.stream().anyMatch(user -> user.getId().equals(loginUserId));

        //나와의 채팅 개설 시도 차단
        if (hasMe) {
            log.warn("[MessageEligibilityPolicy] 대화창 개설 실패 - 자신과 대화창 개설 시도함");
            throw new FMResourceConflictException("자기 자신과는 대화창을 개설할 수 없습니다.");
        }

        //초대 멤버들 한 명씩 빼서 검증
        for (int i = 0; i < targetUsers.size(); i++) {
            UserWithFMJpaEntity targetUser = targetUsers.get(i);
            String friendStatus = friendStatuses.get(i);

            //409 다대다의 경우 학생 외(강사, 관리자) 포함 불가, 비활성 유저 포함 불가
            if (targetUsers.size() > 1){
                if (!"STUDENT".equals(targetUser.getRole()) || !"ACTIVE".equals(targetUser.getStatus())) {
                    log.warn("[MessageEligibilityPolicy] 대화창 개설 실패 - 학생 외, 비활성 인물 초대 불가. 유저ID: {}, 역할: {}, 상태: {}", targetUser.getId(), targetUser.getRole(), targetUser.getStatus());
                    throw new FMResourceConflictException("대화창을 개설할 수 없는 사용자가 포함되어 있습니다.");
                }
            }


            //v2 -> 초대할 멤버와 로그인 유저가 친구인지만 확인(초대된 멤버끼리는 친구 아닐 수 있음)
            //무조건 FRIEND 상태여야 개설 가능
            if (!targetUser.getId().equals(loginUserId) && !"FRIEND".equals(friendStatus)) {
                log.warn("[MessageEligibilityPolicy] 대화창 개설 실패 - 친구 상태가 아닌 사용자가 포함됨. (유저ID: {}, 현재 상태: {})", targetUser.getId(), friendStatus);
                throw new FMResourceConflictException("대화창을 개설할 수 없는 사용자가 포함되어 있습니다.");
            }

        }

    }

    //회원가입 완료 후 이벤트로 나와의 채팅방 개설 검증
    public boolean isSelfChatRoomExists(Long userId) {
        log.info("[MessageEligibilityPolicy] 회원가입 직후 나와의 채팅방 중복 생성 여부 검증 - 유저ID: {}", userId);

        //내가 참여 중인 행이 단 1개라도 있으면 이미 방이 파진 것
        return messageRepository.existsChatRoomMemberByUserId(userId);
    }

    //메시지 전송 검증
    public void sendable(Long roomId, Long senderId, String friendStatus, long roomMemberCount, boolean isOneToOne, UserWithFMJpaEntity targetUser, Long firstRoomId) {

        //로그인한 유저가 채팅방의 멤버가 맞는지 검증
        boolean isMember = messageRepository.existsMemberByRoomIdAndUserId(roomId, senderId);

        if (!isMember) {
            log.warn("[MessageEligibilityPolicy] 권한 없음 - 요청 유저가 방의 멤버가 아님. 유저: {}, 방: {}", senderId, roomId);
            throw new FMResourceAccessDeniedException("해당 채팅방에 접근할 권한이 없습니다.");
        }

        //나와의 채팅방 여부 확인
        Long selfRoomId = messageRepository.findFirstRoomIdByUserId(senderId)
                .orElse(null);
        //나와의 채팅방이라면 상대방 퇴장 및 친구 상태 검증 모두 통과
        if (roomId.equals(selfRoomId)) {
            log.info("[MessageEligibilityPolicy] 나와의 채팅방 메시지 전송 - 검증 패스. 방ID: {}", roomId);
            return;
        }

        //방에 혼자 남았다면 상대방이 나간 것(409)
        //targetUser가 로그인한 유저: 상대가 나간 것 or 나와의 채팅
        //isOneToOne이 true: 일대일 채팅방(나와의 채팅방 포함)
        //나와의 채팅: 로그인 유저의 첫 채팅방
        if (!Objects.equals(roomId, firstRoomId)) {
            if (roomMemberCount < 2) {
            log.warn("[MessageEligibilityPolicy] 메시지 전송 실패 - 상대방이 방을 나갔음. 방ID: {}", roomId);
            throw new FMResourceConflictException("상대방이 채팅방을 나갔습니다.");
            }
        }

        //v2 -> 다대다 분기: 멤버가 3명 이상이면 (로그인 유저 포함) 친구 검증 패스
        if (roomMemberCount >= 3 || !isOneToOne) {
            log.info("[MessageEligibilityPolicy] 다대다 그룹 채팅방 메시지 전송 - 친구 여부 관계없이 허용. 방ID: {}", roomId);
            return;
        }

        //친구 상태가 아니라면 전송 불가(409)
        if (!"FRIEND".equals(friendStatus)) {
            log.warn("[MessageEligibilityPolicy] 메시지 전송 실패 - 차단된 관계. 요청자: {}", senderId);
            throw new FMResourceConflictException("메시지를 전송할 수 없는 사용자입니다.");
        }

        //일대일인 경우 비활성 유저에게 못 보냄.
        if (!"ACTIVE".equals(targetUser.getStatus())) {
            log.warn("[MessageEligibilityPolicy] 메시지 전송 실패 - 상대방이 활성 상태가 아님. 상대 상태:{}", targetUser.getStatus());
            throw new FMBusinessRuleViolationException("상대방이 비활성화 상태이므로 메시지를 보낼 수 없습니다.");
        }

    }

    //메시지 내역 조회 검증
    public void validateAccess(Long roomId, Long userId, boolean isCurrentMember) {
        if (!isCurrentMember) {
            log.warn("[MessageEligibilityPolicy] 접근 권한 없음 - 요청 유저가 방의 멤버가 아님. 유저ID: {}, 방ID: {}", userId, roomId);
            throw new FMResourceAccessDeniedException("해당 채팅방에 접근할 권한이 없습니다.");
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
        List<ChatRoomMemberJpaEntity> myAllRooms = messageRepository.findChatRoomMembersByUserId(userId);
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

    //채팅방 이름 변경(다대다)
    public void modifyRoomTitle(Long roomId, Long userId, String newRoomTitle, ChatRoomJpaEntity chatRoom, List<ChatRoomMemberJpaEntity> members) {

        //해당 방에 멤버인지 권한 확인
        //서비스 클래스에서 가져온 멤버 정보 재활용
        boolean isRoomMember = members.stream()
                .anyMatch(member -> member.getUserId().getId().equals(userId));

        if (!isRoomMember) {
            log.warn("[MessageEligibilityPolicy] 채팅방 이름 변경 실패 - 해당 방 멤버가 아님. 방ID:{}, 유저ID:{}", roomId, userId);
            throw new FMResourceAccessDeniedException("해당 채팅방에 접근할 권한이 없습니다.");
        }
        //접근 권한, 다대다 아님, 똑같은 이름, 빈 값, 20자 제한
        //해당 채팅방이 다대다가 아님
        //방 이름 없음(일대일)
        boolean isOneToOne = chatRoom.getRoomTitle() == null || chatRoom.getRoomTitle().trim().isEmpty();
        //방 멤버수
        long inMemberCount = members.size();
        //다대다 아닌 경우: 채팅방 이름이 없는 일대일 방이면서 멤버가 3명 미만
        if (isOneToOne && inMemberCount < 3) {
            log.warn("[MessageEligibilityPolicy] 채팅방 이름 변경 실패 - 다대다 채팅방이 아님. 방ID:{}, 멤버수:{}", roomId, inMemberCount);
            throw new FMBusinessRuleViolationException("해당 채팅방은 채팅방 이름을 설정할 수 없습니다.(단체 채팅방에 한해서 수정 가능)");
        }

        //빈 값 입력
        if (newRoomTitle == null || newRoomTitle.trim().isEmpty()) {
            log.warn("[MessageEligibilityPolicy] 채팅방 이름 변경 실패 - 공백 입력 시도");
            throw new FMBusinessRuleViolationException("방 제목은 공백일 수 없습니다.");
        }

        //20자 제한
        if (newRoomTitle.length() > 20) {
            log.warn("[MessageEligibilityPolicy] 채팅방 이름 변경 실패 - 20자 제한 초과 입력. 입력된 글자수: {}", newRoomTitle.length());
            throw new FMBusinessRuleViolationException("방 제목은 최대 20자까지 가능합니다.");
        }

        //기존 방 이름과 동일
        if (newRoomTitle.equals(chatRoom.getRoomTitle())) {
            log.warn("[MessageEligibilityPolicy] 채팅방 이름 변경 실패 - 변동 사항 없음. 기존: {}, 입력: {}", newRoomTitle, chatRoom.getRoomTitle());
            throw new FMBusinessRuleViolationException("변경 사항이 없어 수정되지 않습니다.");
        }
    }

    //다대다 채팅방 멤버 초대하기
    //멤버 초대 기본 검증 - for문에서 중복 검증하는 거 따로 빼기.
    public void validateBeforeLoop(ChatRoomJpaEntity chatRoom, Long userId, List<Long> chatMember) {
        //빈 값 입력 시 예외
        if (chatMember == null || chatMember.isEmpty()) {
            log.warn("[MessageEligibilityPolicy] 채팅방 멤버 초대 실패 - 초대 멤버를 선택하지 않음");
            throw new FMBusinessRuleViolationException("초대할 대상을 선택해주세요.");
        }

        //로그인 유저가 방 멤버인지 확인
        if (!messageRepository.existsMemberByRoomIdAndUserId(chatRoom.getId(), userId)) {
            log.warn("[MessageEligibilityPolicy] 채팅방 멤버 초대 실패 - 로그인 유저가 참여 중인 방이 아님. 방ID:{}, 유저ID:{}", chatRoom.getId(), userId);
            throw new FMResourceAccessDeniedException("해당 채팅방에 접근할 권한이 없습니다.");
        }

        //중복 멤버 포함
        long distinctCount = chatMember.stream().distinct().count();
        if (distinctCount != chatMember.size()) {
            log.warn("[MessageEligibilityPolicy] 채팅방 멤버 초대 실패 - 중복된 사용자 초대.");
            throw new FMBusinessRuleViolationException("초대 대상자 중 중복된 사용자가 포함되어 있습니다.");
        }
    }
    public void inviteRoomMember(ChatRoomJpaEntity chatRoom, Long userId, List<Long> chatMember, boolean hasMe, String friendStatus, boolean isExistMember, boolean isNotStudentOrActive) {
        // 로그인 유저(초대 주체)와 초대 대상자들이 친구인지 검증, 일대일인지 확인, 중복 멤버 확인

        //일대일 방인지 확인
        if (chatRoom.getRoomTitle() == null || chatRoom.getRoomTitle().trim().isEmpty()) {
            log.warn("[MessageEligibilityPolicy] 채팅방 멤버 초대 실패 - 일대일 채팅에서 초대 시도함. 방ID: {}", chatRoom.getId());
            throw new FMBusinessRuleViolationException("채팅방 멤버 초대는 단체 채팅방에서만 가능합니다.");
        }

        //초대 대상자가 이미 멤버인지 확인
        if (isExistMember) {
            log.warn("[MessageEligibilityPolicy] 채팅방 멤버 초대 실패 - 초대 대상자 중 이미 멤버인 유저 존재.");
            throw new FMBusinessRuleViolationException("이미 방에 참여중인 멤버가 포함되어 있습니다.");
        }

        // 본인 초대 불가
        if (hasMe) {
            log.warn("[MessageEligibilityPolicy] 채팅방 멤버 초대 실패 - 본인 초대. 방ID:{}, 유저ID:{}", chatRoom.getId(), userId);
            throw new FMBusinessRuleViolationException("자기 자신은 초대할 수 없습니다.");
        }

        //초대 대상자가 학생 아닌 경우(강사, 관리자) + 비활성 유저
        if (isNotStudentOrActive) {
            log.warn("[MessageEligibilityPolicy] 채팅방 멤버 초대 실패 - 학생 아닌 유저 초대 시도함.");
            throw new FMBusinessRuleViolationException("초대할 수 없는 사용자가 포함되어 있습니다.");
        }


        //로그인 유저와 미친구
        if (!"FRIEND".equals(friendStatus)) {
            log.warn("[MessageEligibilityPolicy] 채팅방 멤버 초대 실패 - 로그인 유저와 친구 상태가 아님. 상태: {}", friendStatus);
            throw new FMBusinessRuleViolationException("친구 상태인 사용자만 초대할 수 있습니다.");
        }
    }

}
