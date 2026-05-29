package com.wanted.momocity.message.application.service;

import com.wanted.momocity.friend.user.UserWithFMJpaEntity;
import com.wanted.momocity.message.application.policy.MessageEligibilityPolicy;
import com.wanted.momocity.message.domain.repository.MessageRepository;
import com.wanted.momocity.message.infrastructure.persistence.*;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional
public class MessageHandlerService {

    private final MessageRepository messageRepository;
    private final MessageSideUserRepository messageSideUserRepository;
    private final MessageEligibilityPolicy messageEligibilityPolicy;

    private final SpringDataChatRoomRepository springDataChatRoomRepository;
    private final SpringDataChatRoomMemberRepository springDataChatRoomMemberRepository;
    private final SpringDataMessageRepository springDataMessageRepository;

    //회원가입 성공 후 날라온 이벤트로 나와의 채팅방 최초 1회 생성
    public void createSelfChatRoom(Long userId) {
        log.info("[MessageHandlerService] 나와의 채팅방 개설 시작 - 대상 유저ID: {}", userId);

        //회원가입 직후 이미 방이 파진 흔적이 있다면 중복 생성이므로 넘어감
        if (messageEligibilityPolicy.isSelfChatRoomExists(userId)) {
            log.warn("[MessageHandlerService] 이미 나와의 채팅방이 존재하여 생성을 건너뜀. (중복 이벤트 방어)");
            return;
        }

        //신규 채팅방 개설(생성 시간 포함)
        ChatRoomJpaEntity selfRoom = new ChatRoomJpaEntity();
        messageRepository.saveChatRoom(selfRoom);

        //가입 완료된 내 유저 정보
        UserWithFMJpaEntity me = messageSideUserRepository.getReferenceById(userId);

        //채팅방에 멤버 딱 한 명
        ChatRoomMemberJpaEntity selfMembership = ChatRoomMemberJpaEntity.createMembership(selfRoom, me);
        messageRepository.saveChatRoomMember(selfMembership);

        log.info("[MessageHandlerService] 나와의 채팅방 생성 및 멤버 저장 완료 - 방ID: {}, 유저ID: {}", selfRoom.getId(), me.getId());
    }

    //친구 삭제 후 채팅방 나가기
    public void leaveChatRoom(Long userId, Long targetUserId) {
        log.info("[MessageHandlerService] 친구 삭제 이벤트 수신 -> 채팅방 퇴장 처리 시작 - 요청자: {}, 대상자: {}", userId, targetUserId);

        // 1. 두 유저가 동시에 참여하고 있는 1:1 채팅방 ID 찾아오기
        // (JPA 규칙이나 편의를 위해 member 테이블에서 나(userId)의 방 목록 중 상대방(targetUserId)이 껴있는 방을 조회합니다)
        List<ChatRoomMemberJpaEntity> myRooms = springDataChatRoomMemberRepository.findByUserId_Id(userId);
        Long foundRoomId = null;

        for (ChatRoomMemberJpaEntity myRoom : myRooms) {
            Long roomId = myRoom.getRoomId().getId();

            //상대방이 먼저 나간 경우 메시지로 확인
            boolean hasHistory = springDataMessageRepository.existsByRoomId_IdAndSenderId_Id(roomId, targetUserId);

            // 해당 방의 전체 멤버를 긁어서 상대방이 포함되어 있는지 확인
            List<ChatRoomMemberJpaEntity> roomMembers = springDataChatRoomMemberRepository.findByRoomId_Id(roomId);
            for (ChatRoomMemberJpaEntity member : roomMembers) {
                if (member.getUserId().getId().equals(targetUserId)) {
                    foundRoomId = roomId;
                    break;
                }
            }
            if (foundRoomId == null && hasHistory) {
                foundRoomId = roomId;
            }

            if (foundRoomId != null) break;
        }

        // 만약 두 사람 사이에 개설된 채팅방이 없다면 나갈 일도 없으니 가볍게 종료
        if (foundRoomId == null) {
            log.info("[MessageHandlerService] 두 유저 사이에 활성화된 채팅방이 존재하지 않아 퇴장 처리를 스킵합니다.");
            return;
        }

        // 2. 해당 방의 찐 전체 멤버 다시 확보
        List<ChatRoomMemberJpaEntity> allMembers = springDataChatRoomMemberRepository.findByRoomId_Id(foundRoomId);
        ChatRoomJpaEntity chatRoom = springDataChatRoomRepository.findById(foundRoomId).orElse(null);

        // 3. 내 멤버 정보(삭제 대상) 솎아내기
        ChatRoomMemberJpaEntity myMembership = null;
        for (ChatRoomMemberJpaEntity member : allMembers) {
            if (member.getUserId().getId().equals(userId)) {
                myMembership = member;
                break;
            }
        }

        if (myMembership == null) return; // 방어 코드

        int currentMemberCount = allMembers.size();

        // [분기 케이스 A]: 내가 이 방의 마지막 남은 사용자일 때 ➡️ 방 완전히 폭파
        if (currentMemberCount <= 1) {
            log.info("[MessageHandlerService] 마지막 사용자 퇴장 -> 방 완전히 폭파. 방ID: {}", foundRoomId);
            springDataMessageRepository.deleteByRoomId_Id(foundRoomId);
            springDataChatRoomMemberRepository.delete(myMembership);
            if (chatRoom != null) {
                springDataChatRoomRepository.delete(chatRoom);
            }
            return;
        }

        // [분기 케이스 B]: 상대방이 남아있을 때 ➡️ 나만 나가기 (chat_room_member에서 나만 삭제)
        // 이렇게 하면 남겨진 사람 입장에선 방과 기존 내역이 다 보이지만, 전송 검증(Ensure) 시점에
        // 튕겨나가고 상대방 유저 상태에 따라 (알 수 없음) 가공 처리가 유기적으로 발동합니다!
        log.info("[MessageHandlerService] 상대방이 존재함 -> 삭제자 멤버 데이터만 제거. 방ID: {}", foundRoomId);
        springDataChatRoomMemberRepository.delete(myMembership);
    }
}
