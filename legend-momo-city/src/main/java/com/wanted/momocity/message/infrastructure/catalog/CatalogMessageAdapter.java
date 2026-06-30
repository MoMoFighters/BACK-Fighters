package com.wanted.momocity.message.infrastructure.catalog;

import com.wanted.momocity.friend.enrollment.EnrollmentWithFMJpaEntity;
import com.wanted.momocity.friend.infrastructure.persistence.FriendJpaEntity;
import com.wanted.momocity.friend.user.UserWithFMJpaEntity;
import com.wanted.momocity.message.domain.repository.ChatRoomQueryProjection;
import com.wanted.momocity.message.domain.repository.MessageRepository;
import com.wanted.momocity.message.infrastructure.persistence.*;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import javax.swing.text.html.Option;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

import static org.apache.logging.log4j.ThreadContext.isEmpty;

//포트 문을 통해 db세상으로 나가는 문
//다른 테이블에서 필요한 정보 가져오기(또는 서비스에서)
@Repository
@Transactional(readOnly = true)
@RequiredArgsConstructor
@Slf4j
public class CatalogMessageAdapter implements MessageRepository {

    private final MessageSideUserRepository messageSideUserRepository;
    private final MessageSideEnrollmentRepository messageSideEnrollmentRepository;
    private final MessageSideFriendRepository messageSideFriendRepository;

    private final SpringDataChatRoomMemberRepository springDataChatRoomMemberRepository;
    private final SpringDataMessageRepository springDataMessageRepository;
    private final SpringDataChatRoomRepository springDataChatRoomRepository;
    private final SpringDataMessageReadRepository springDataMessageReadRepository;
    private final SpringDataMessageAnnounceRepository springDataMessageAnnounceRepository;

    //사용자 정보 찾기
    @Override
    public Optional<UserWithFMJpaEntity> findUserWithFMById(Long userId) {
        return messageSideUserRepository.findUserWithFMById(userId);
    }

    //수강 정보 찾기
    @Override
    public List<EnrollmentWithFMJpaEntity> findEnrollmentsByUserId(Long userId) {
        return messageSideEnrollmentRepository.findByUserId_Id(userId);
    }

    //나간 상대방 식별 메시지 역추적용(로그인 유저 제외 마지막 메시지 정보)
    @Override
    public Optional<MessageJpaEntity> findLatestMessageExceptMe(Long roomId, Long loginUserId) {
        return springDataMessageRepository.findFirstByRoomId_IdAndSenderId_IdNotOrderByIdDesc(roomId, loginUserId);
    }

    //양방향 친구 관계 찾기
    @Override
    public Optional<FriendJpaEntity> findFriendRelation(Long loginId, Long targetUserId) {
        Optional<FriendJpaEntity> relation = messageSideFriendRepository.findByFromUserId_IdAndToUserId_Id(loginId, targetUserId);
        if (relation.isEmpty()) {
            relation = messageSideFriendRepository.findByFromUserId_IdAndToUserId_Id(targetUserId, loginId);
        }
        return relation;
    }

    //채팅방 정렬을 위한 마지막 안내 문구 시간
    @Override
    public Optional<LocalDateTime> findLatestAnnounceTime(Long roomId) {
        return springDataMessageAnnounceRepository.findFirstByRoomId_IdOrderByCreatedAtDesc(roomId)
                .map(MessageAnnounceJpaEntity::getCreatedAt);
    }

    //채팅방 존재 확인
    @Override
    public boolean existsRoomById(Long roomId) {
        return springDataChatRoomRepository.existsById(roomId);
    }

    //방 멤버 존재 확인
    @Override
    public boolean existsMemberByRoomIdAndUserId(Long roomId, Long userId) {
        return springDataChatRoomMemberRepository.existsByRoomId_IdAndUserId_Id(roomId, userId);
    }

    //마지막 메시지 존재 확인
    @Override
    public boolean existsMessageByRoomIdAndSenderId(Long roomId, Long senderId) {
        return springDataMessageRepository.existsByRoomId_IdAndSenderId_Id(roomId, senderId);
    }

    //20개씩 메시지 내역 조회
    @Override
    public List<MessageJpaEntity> findMessageHistory(Long roomId, Long lastMessageId, LocalDateTime startTimeLie) {
        if (lastMessageId == null) {
            return springDataMessageRepository.findTop20ByRoomId_IdAndCreatedAtGreaterThanEqualOrderByIdDesc(roomId, startTimeLie);
        }
        return springDataMessageRepository.findTop20ByRoomId_IdAndIdLessThanAndCreatedAtGreaterThanEqualOrderByIdDesc(roomId, lastMessageId, startTimeLie);
    }

    //프록시
    @Override
    public UserWithFMJpaEntity getUserWithFMReferenceById(Long userId) {
        return messageSideUserRepository.getReferenceById(userId);
    }

    //로그인 유저가 속한 방 멤버
    @Override
    public List<ChatRoomMemberJpaEntity> findChatRoomMembersByUserId(Long userId) {
        return springDataChatRoomMemberRepository.findByUserId_Id(userId);
    }

    //메시지 읽음 처리
    @Override
    @Transactional
    public void saveAllMessageRead(List<MessageReadJpaEntity> messageReads) {
        springDataMessageReadRepository.saveAll(messageReads);
    }

    //안읽은 메시지 내역 조회
    @Override
    public List<MessageReadJpaEntity> findUnreadMessages(Long roomId, Long userId) {
        return springDataMessageReadRepository.findByRoomId_IdAndUserId_IdAndIsMsgReadFalse(roomId, userId);
    }

    //채팅방 나가기: 모든 메시지 삭제
    @Override
    @Transactional
    public void deleteMessagesByRoomId(Long roomId) {
        springDataMessageAnnounceRepository.deleteByRoomId_Id(roomId);
        springDataMessageReadRepository.deleteByRoomId_Id(roomId);
        springDataMessageRepository.deleteByRoomId_Id(roomId);
    }

    //채팅방 나가기: 멤버 삭제
    @Override
    @Transactional
    public void deleteChatRoomMember(ChatRoomMemberJpaEntity member) {
        springDataChatRoomMemberRepository.delete(member);
    }

    //채팅방 나가기: 방 삭제
    @Override
    @Transactional
    public void deleteChatRoom(ChatRoomJpaEntity room) {
        springDataChatRoomRepository.delete(room);
    }

    //로그인 유저의 채팅방 조회
    @Override
    public List<ChatRoomQueryProjection> findChatRoomByUserId(Long userId) {
        log.info("[CatalogMessageAdapter] 로그인 유저의 채팅방 데이터 원천 조회 - 유저ID: {}", userId);

        //로그인 유저가 참여중인 방 목록들을 멤버 테이블에 가져옴
        List<ChatRoomMemberJpaEntity> myMemberships = springDataChatRoomMemberRepository.findByUserId_Id(userId);
        List<ChatRoomQueryProjection> result = new ArrayList<>();

        for (ChatRoomMemberJpaEntity membership : myMemberships) {
            ChatRoomJpaEntity room = membership.getRoomId();
            Long roomId = membership.getRoomId().getId();

            //각 방의 마지막 한 문장 추출
            Optional<MessageJpaEntity> lastMsgOpt = springDataMessageRepository.findFirstByRoomId_IdOrderByIdDesc(roomId);

            MessageJpaEntity finalMessage;

            if (lastMsgOpt.isPresent()) {
                //진짜 메시지가 있으면 그대로 넣기
                finalMessage = lastMsgOpt.get();
            } else {
                //노출용 가짜 메시지 객체 조립
                finalMessage = new MessageJpaEntity();

                //메시지와 시간만 채우기
                finalMessage.changeContent("새로운 채팅방이 개설되었습니다. 첫 메시지를 보내보세요!");
                //정렬에서 안터지도록 방 생성 시간 넣음
                finalMessage.changeCreatedAt(room.getCreatedAt());
            }

            result.add(new ChatRoomQueryProjection(
                    roomId, room.getRoomTitle(), finalMessage, room.getCreatedAt()
            ));
        }

        log.info("[CatalogMessageAdapter] 채팅 목록 조회 완료 - 채팅방 개수: {}개", result.size());
        return result;
    }

    //채팅방별 안읽은 메시지 개수
    @Override
    public Long countUnreadMessage(Long roomId, Long userId) {
        return springDataMessageReadRepository.countByRoomId_IdAndUserId_IdAndIsMsgReadFalse(roomId, userId);
    }

    //채팅방 조회 및 신설
    //기존 채팅방 존재 확인
    @Override
    public Optional<Long> findExistingRoom(Long userId, Long targetUserId) {
        log.info("[CatalogMessageAdapter] 두 유저간의 기존 대화방 존재 여부 탐색 시작 - 요청자ID: {}, 대상자ID: {}", userId, targetUserId);

        //두 유저가 참여중인 방ID
        List<ChatRoomMemberJpaEntity> memberships = springDataChatRoomMemberRepository.findByUserId_IdInAndRoomId_RoomTitleIsNull(List.of(userId, targetUserId));

        // 2. 엔티티 리스트에서 '방 ID'만 추출해서 Long 리스트로 변환합니다.
        List<Long> roomIds = memberships.stream()
                .map(m -> m.getRoomId().getId())
                .toList();

        //교집합: 빈도수가 2인 것 가져오기.
        // 나의 모든 일대일 채팅방, 상대의 모든 일대일 채팅방 중 두 번 나온게 나와 상대방 유저의 일대일 방
        return roomIds.stream()
                .filter(roomId -> Collections.frequency(roomIds, roomId) == 2)
                .findFirst();
    }

    //채팅방 개설
    //채팅방 생성 시점에 시간 주입
    @Override
    @Transactional
    public void saveChatRoom(ChatRoomJpaEntity room) {
        room.changeCreatedAt(LocalDateTime.now());
        room.changeUpdatedAt(LocalDateTime.now());
        springDataChatRoomRepository.save(room);
        log.info("[CatalogMessageAdapter] chat_room 테이블 행 추가 완료 - 채팅방생성시간: {}", room.getCreatedAt());
    }
    //멤버 저장
    @Override
    @Transactional
    public void saveChatRoomMember(ChatRoomMemberJpaEntity member) {
        springDataChatRoomMemberRepository.save(member);
        log.info("[CatalogMessageAdapter] chat_room_member 행 추가 완료 - 매핑 방ID: {}", member.getRoomId().getId());
    }

    //메시지 전송
    //채팅방 단건 조회
    @Override
    public Optional<ChatRoomJpaEntity> findChatRoomById(Long roomId) {
        log.info("[CatalogMessageAdapter] 채팅방 단건 조회 - 방ID: {}", roomId);

        return springDataChatRoomRepository.findById(roomId);
    }
    //멤버 조회
    @Override
    public List<ChatRoomMemberJpaEntity> findMembersByRoomId(Long roomId) {
        log.info("[CatalogMessageAdapter] 특정 채팅방의 멤버 목록 조회 - 방ID: {}", roomId);

        return springDataChatRoomMemberRepository.findByRoomId_Id(roomId);
    }

    //메시지 저장
    @Override
    @Transactional
    public void saveMessage(MessageJpaEntity newMessage) {
        log.info("[CatalogMessageAdapter] message 테이블에 저장 - 요청자ID: {}", newMessage.getSenderId().getId());

        springDataMessageRepository.save(newMessage);
    }

    //(말풍선)하나의 메시지에 대해 읽지 않은 사람 수
    @Override
    public Long countUnreadMembersForMessage(Long messageId) {
        //메시지 읽음 테이블에서 해당 메시지 아이디 기준으로 isMsgRead가 false인 개수
        return springDataMessageReadRepository.countByMessageId_IdAndIsMsgReadFalse(messageId);
    }

    //안내 문구 조회
    @Override
    public List<MessageAnnounceJpaEntity> findAnnounceHistory(Long roomId, LocalDateTime startTimeLine, LocalDateTime endTimeLine) {
        //채팅방 생성 또는 멤버로 가입된 시점 이후부터 마지막 메시지의 시간 사이의 안내 문구 조회
        return springDataMessageAnnounceRepository.findByRoomId_IdAndCreatedAtBetween(roomId, startTimeLine, endTimeLine);
    }

    //회원가입 직후 방 존재 여부 확인용
    @Override
    public boolean existsChatRoomMemberByUserId(Long userId) {
        return springDataChatRoomMemberRepository.existsByUserId_Id(userId);
    }

    //나와의 채팅방 인식: 첫 번째 방
    @Override
    public Optional<Long> findFirstRoomIdByUserId(Long userId) {
        List<ChatRoomMemberJpaEntity> myAllRooms = springDataChatRoomMemberRepository.findByUserId_Id(userId);
        return myAllRooms.stream()
                .map(member -> member.getRoomId().getId())
                .min(Long::compare);
    }

    //채팅방 나가기: 안내문구 저장
    @Override
    @Transactional
    public void saveLeaveAnnounce(ChatRoomJpaEntity chatRoom, UserWithFMJpaEntity leaveUser, String leaveMessage) {
        log.info("[CatalogMessageAdapter] 채팅방 퇴장 안내 문구 저장 시작 - 방ID:{}, 유저ID:{}", chatRoom.getId(), leaveUser.getId());

        MessageAnnounceJpaEntity announce = MessageAnnounceJpaEntity.createAnnounce(
                chatRoom,
                leaveUser,
                leaveMessage,
                "LEAVE",
                LocalDateTime.now()
        );
        springDataMessageAnnounceRepository.save(announce);
        log.info("[CatalogMessageAdapter] 퇴장 안내 문구 저장 완료 - ID: {}", announce.getId());

    }

    //채팅방 목록 조회: 마지막 메시지 시간
    @Override
    public Optional<LocalDateTime> findLatestMessageTimeById(Long messageId) {
        return springDataMessageRepository.findCreatedAtById(messageId);
    }

    //웹소켓 메시지 내역 조회?(지연 가능성)
    @Override
    public void fastSaveChanges() {
        springDataMessageReadRepository.flush();
    }

    //일대일 재입장 안내 문구
    @Override
    @Transactional
    public void saveEnterAnnounce(ChatRoomJpaEntity chatRoom, UserWithFMJpaEntity enterUser, String enterMessage) {
        log.info("[CatalogMessageAdapter] 일대일 채팅방 재입장 안내 문구 저장 시작 - 방ID:{}, 유저ID:{}", chatRoom.getId(), enterUser.getId());
        MessageAnnounceJpaEntity announce = MessageAnnounceJpaEntity.createAnnounce(
                chatRoom,
                enterUser,
                enterMessage,
                "INVITE",
                LocalDateTime.now()
        );
        springDataMessageAnnounceRepository.save(announce);
        log.info("[CatalogMessageAdapter] 일대일 재입장 안내 문구 저장 완료 - ID: {}", announce.getId());
    }

    //친구 삭제 핸들러 - 상대가 나간 채팅방 안내 문구 검증
    @Override
    public boolean existsAnnounceByRoomIdAndTargetId(ChatRoomJpaEntity room, UserWithFMJpaEntity targetUser) {
        return springDataMessageAnnounceRepository.existsAnnounceByRoomId_IdAndTargetId_Id(room.getId(), targetUser.getId());
    }

    //채팅방 목록 조회 - 상대가 나간 채팅방 안내 문구 검증
    @Override
    public Optional<UserWithFMJpaEntity> findLatestAnnounceUser(Long roomId) {
        log.info("[CatalogMessageAdapter] 메시지 없는 방 역추적 - 최신 안내 문구 분석 중. 방ID: {}", roomId);
        return springDataMessageAnnounceRepository.findFirstByRoomId_IdOrderByCreatedAtDesc(roomId)
                .map(MessageAnnounceJpaEntity::getTargetId);
    }

    @Override
    @Transactional
    public void saveRenameAnnounce(ChatRoomJpaEntity chatRoom, UserWithFMJpaEntity loginUser, String announceContent, LocalDateTime updatedAt) {
        log.info("[CatalogMessageAdapter] 채팅방 이름 수정 저장 시작 - 방ID:{}, 수정 주체ID: {}", chatRoom.getId(), loginUser.getId());
        MessageAnnounceJpaEntity announce = MessageAnnounceJpaEntity.createAnnounce(
                chatRoom,
                loginUser,
                announceContent,
                "RENAME",
                updatedAt
        );
        springDataMessageAnnounceRepository.save(announce);
        log.info("[CatalogMessageAdapter] 다대다 방이름 수정 안내 문구 저장 완료 - ID: {}", announce.getId());
    }

    //다대다 초대자들 저장
    @Override
    @Transactional
    public void saveInviteChatRoomMember(ChatRoomJpaEntity room, UserWithFMJpaEntity invitedUser, LocalDateTime joinedAt) {
        log.info("[CatalogMessageAdapter] 다대다 채팅방 멤버 초대자들 저장 시작 - 방ID:{}", room.getId());
        ChatRoomMemberJpaEntity newInviteMember = ChatRoomMemberJpaEntity.createInviteMembership(
                room,
                invitedUser,
                joinedAt
        );
        springDataChatRoomMemberRepository.save(newInviteMember);
        log.info("[CatalogMessageAdapter] 다대다 채팅방 멤버 초대자 저장 완료.");
    }

    //다대다 채팅방 멤버 초대 안내 문구
    @Override
    @Transactional
    public void saveInviteAnnounce(ChatRoomJpaEntity chatRoom, UserWithFMJpaEntity loginUser, String inviteMessage, LocalDateTime createdAt) {
        log.info("[CatalogMessageAdapter] 채팅방 멤버 초대 안내 문구 저장 시작 - 방ID:{}, 초대 주체ID: {}", chatRoom.getId(), loginUser.getId());
        MessageAnnounceJpaEntity announce = MessageAnnounceJpaEntity.createAnnounce(
                chatRoom,
                loginUser,
                inviteMessage,
                "INVITE",
                createdAt
        );
        springDataMessageAnnounceRepository.save(announce);
        log.info("[CatalogMessageAdapter] 채팅방 멤버 초대 안내 문구 저장 완료 - ID: {}", announce.getId());
    }

    //친구 삭제 후 채팅방 나가기 버그 수정
    @Override
    public Optional<Long> findOneToOneChatRoomIdBetween(Long userId, Long targetUserId) {
        return springDataChatRoomMemberRepository.findOneToOneChatRoomIdBetween(userId, targetUserId);
    }
    //친구 삭제 후 채팅방 나가기 버그 수정
    @Override
    public Optional<ChatRoomMemberJpaEntity> findMemberByRoomIdAndUserId(Long foundRoomId, Long userId) {
        return springDataChatRoomMemberRepository.findMemberByRoomIdAndUserId(foundRoomId, userId);
    }

    //채팅방 목록 조회 개선 보강
    @Override
    public List<ChatRoomMemberJpaEntity> findByRoomId_IdIn(List<Long> allRoomIds) {
        return springDataChatRoomMemberRepository.findByRoomId_IdIn(allRoomIds);
    }
    //채팅방 목록 조회 개선 보강
    @Override
    public List<Object[]> findLatestAnnounceTimeByRoomIdsIn(List<Long> allRoomIds) {
        return springDataMessageAnnounceRepository.findLatestAnnounceTimeByRoomIdsIn(allRoomIds);
    }
    //채팅방 목록 조회 개선 보강
    @Override
    public List<EnrollmentWithFMJpaEntity> findByUserId_IdIn(List<Long> longs) {
        return messageSideEnrollmentRepository.findByUserId_IdIn(longs);
    }

    //채팅방 조회 및 개설 개선 보강
    @Override
    public List<ChatRoomMemberJpaEntity> findOnePersonRoomsByUserId(Long targetUserId, Long loginUserId) {
        return springDataChatRoomMemberRepository.findOnePersonRoomsByUserId(targetUserId, loginUserId);
    }

}
