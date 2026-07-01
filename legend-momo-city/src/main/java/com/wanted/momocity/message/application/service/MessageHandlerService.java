package com.wanted.momocity.message.application.service;

import com.wanted.momocity.friend.user.UserWithFMJpaEntity;
import com.wanted.momocity.message.application.policy.MessageEligibilityPolicy;
import com.wanted.momocity.message.application.query.FindChatRoomQuery;
import com.wanted.momocity.message.application.query.GetMessageHistoryQuery;
import com.wanted.momocity.message.application.usecase.MessageQueryUseCase;
import com.wanted.momocity.message.domain.repository.MessageRepository;
import com.wanted.momocity.message.infrastructure.persistence.*;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional
public class MessageHandlerService {

    private final MessageRepository messageRepository;
    private final MessageEligibilityPolicy messageEligibilityPolicy;

    //친구 삭제로 채팅방 나가기 안내 문구 웹소켓
    private final MessageQueryUseCase messageQueryUseCase; // 웹소켓용 페이로드 조회를 위한 유스케이스
    private final SimpMessagingTemplate messagingTemplate; // 실시간 웹소켓 발송을 위한 템플릿

    //회원가입 성공 후 날라온 이벤트로 나와의 채팅방 최초 1회 생성
    //v2 -> 결제 완료 시 나와의 채팅방 생성으로 변경?
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
        UserWithFMJpaEntity me = messageRepository.getUserWithFMReferenceById(userId);

        //채팅방에 멤버 딱 한 명
        ChatRoomMemberJpaEntity selfMembership = ChatRoomMemberJpaEntity.createMembership(selfRoom, me);
        messageRepository.saveChatRoomMember(selfMembership);

        log.info("[MessageHandlerService] 나와의 채팅방 생성 및 멤버 저장 완료 - 방ID: {}, 유저ID: {}", selfRoom.getId(), me.getId());
    }

    //친구 삭제 후 채팅방 나가기
    public void leaveChatRoom(Long userId, Long targetUserId) {
        log.info("[MessageHandlerService] 친구 삭제 이벤트 수신 -> 채팅방 퇴장 처리 시작 - 요청자: {}, 대상자: {}", userId, targetUserId);

        // 1. 내가 참여 중이면서 상대가 같이 있고, 방 이름이 없는 1:1 방 ID를 DB에서 바로 찾기
        Long foundRoomId = messageRepository.findOneToOneChatRoomIdBetween(userId, targetUserId).orElse(null);

        // 만약 그 친구와 단둘이 쓰던 활성화된 1:1 채팅방이 없다면 나갈 것도 없으니 즉시 종료
        if (foundRoomId == null) {
            log.info("[MessageHandlerService] 해당 친구와 활성화된 1:1 채팅방이 존재하지 않아 퇴장 처리를 스킵합니다.");
            return;
        }

        // 2. 그 방에서 '나(userId)'의 멤버십 정보 딱 하나만 가져오기
        ChatRoomMemberJpaEntity myMembership = messageRepository.findMemberByRoomIdAndUserId(foundRoomId, userId).orElse(null);
        ChatRoomJpaEntity chatRoom = messageRepository.findChatRoomById(foundRoomId).orElse(null);

        if (myMembership == null || chatRoom == null) {
            log.warn("[MessageHandlerService] 채팅방 정보(ID:{}) 또는 멤버십 데이터가 DB에 존재하지 않아 퇴장 처리를 중단.", foundRoomId);
            return; // 방어 코드
        }

        // 3. [나만 나가기 처리] 내 멤버 데이터만 삭제
        log.info("[MessageHandlerService] 1:1 방(ID:{}) 확인 완료 -> 나(ID:{})의 참여 데이터만 제거합니다.", foundRoomId, userId);
        messageRepository.deleteChatRoomMember(myMembership);


        UserWithFMJpaEntity loginUser = myMembership.getUserId();

        //채팅방 나갔다는 안내 문구 message_announce 테이블에 행추가
        messageRepository.saveLeaveAnnounce(chatRoom, loginUser, loginUser.getNickname() + "님이 나갔습니다.");
        log.info("[MessageHandlerService] 친구 삭제로 채팅방 나가기 이벤트 발행 -> message_announce 테이블에 안내 문구 추가. 방ID: {}", foundRoomId);

        // 🌟 [추가] 시간 낭비 없이 웹소켓 버그만 해결하기!
        // 상대방(targetUserId) 화면에 실시간으로 반영되도록 템플릿으로 쏴버립니다.
        String destination = "/sub/chat/room/" + foundRoomId;

        // 1) 상대방 채팅방 내부 메시지 내역 새로고침 데이터 발송
        List<MessageQueryUseCase.MessageHistoryView> historyPayload =
                messageQueryUseCase.getMessageHistoryQueryHandle(new GetMessageHistoryQuery(foundRoomId, targetUserId, null));
        messagingTemplate.convertAndSendToUser(targetUserId.toString(), destination, historyPayload);

        // 2) 상대방 전체 채팅방 리스트 화면 새로고침 데이터 발송
        List<MessageQueryUseCase.ChatRoomView> chatRoomListPayload =
                messageQueryUseCase.getChatRoomQueryHandle(new FindChatRoomQuery(targetUserId));
        messagingTemplate.convertAndSendToUser(targetUserId.toString(), "/sub/chat/rooms", chatRoomListPayload);
        log.info("[MessageHandlerService] 친구 삭제로 채팅방 나가기 완료 및 상대방(ID:{})에게 웹소켓 전송 완료", targetUserId);
    }
}

