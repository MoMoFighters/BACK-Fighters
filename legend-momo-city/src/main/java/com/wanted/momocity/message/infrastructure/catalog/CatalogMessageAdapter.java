package com.wanted.momocity.message.infrastructure.catalog;

import com.wanted.momocity.friend.domain.repository.FriendRepository;
import com.wanted.momocity.friend.infrastructure.persistence.FriendJpaEntity;
import com.wanted.momocity.friend.infrastructure.persistence.FriendSideEnrollmentRepository;
import com.wanted.momocity.friend.infrastructure.persistence.FriendSideUserRepository;
import com.wanted.momocity.friend.infrastructure.persistence.SpringDataFriendRepository;
import com.wanted.momocity.message.domain.repository.ChatRoomQueryProjection;
import com.wanted.momocity.message.domain.repository.MessageRepository;
import com.wanted.momocity.message.infrastructure.persistence.*;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

//포트 문을 통해 db세상으로 나가는 문
//다른 테이블에서 필요한 정보 가져오기(또는 서비스에서)
@Repository
@Transactional(readOnly = true)
@RequiredArgsConstructor
@Slf4j
public class CatalogMessageAdapter implements MessageRepository {

    private final SpringDataFriendRepository springDataFriendRepository;
    private final SpringDataChatRoomMemberRepository springDataChatRoomMemberRepository;
    //충돌 회피로 만든 수강신청 인터페이스 저장소
    private final MessageSideEnrollmentRepository messageSideEnrollmentRepository;
    //충돌 회피로 만든 사용자 인터페이스 저장소
    private final MessageSideUserRepository messageSideUserRepository;
    private final SpringDataMessageRepository springDataMessageRepository;
    private final SpringDataChatRoomRepository springDataChatRoomRepository;

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
                    roomId, finalMessage
            ));
        }

        log.info("[CatalogMessageAdapter] 채팅 목록 조회 완료 - 채팅방 개수: {}개", result.size());
        return result;
    }

    //채팅방별 안읽은 메시지 개수
    @Override
    public Long countUnreadMessage(Long roomId, Long userId) {
        return springDataMessageRepository.countByRoomId_IdAndSenderId_IdNotAndIsReadFalse(roomId, userId);
    }

    //채팅방 조회 및 신설
    //기존 채팅방 존재 확인
    @Override
    public Optional<Long> findExistingRoom(Long userId, Long targetUserId) {
        log.info("[CatalogMessageAdapter] 두 유저간의 기존 대화방 존재 여부 탐색 시작 - 요청자ID: {}, 대상자ID: {}", userId, targetUserId);

        //로그인 유저가 참여하는 방ID
        List<ChatRoomMemberJpaEntity> myMemberships = springDataChatRoomMemberRepository.findByUserId_Id(userId);
        List<Long> myRoomIds = myMemberships.stream()
                .map(m -> m.getRoomId().getId())
                .toList();

        //1차 비교: 멤버에 로그인 유저와 상대방 있는지 확인(나감 여부 없음)
        List<ChatRoomMemberJpaEntity> targetMemberships = springDataChatRoomMemberRepository.findByUserId_Id(targetUserId);

        //로그인 유저와 상대방이 참여한 교집합 방 있으면 반환
        for (ChatRoomMemberJpaEntity targetMeta : targetMemberships) {
            Long targetRoomId = targetMeta.getRoomId().getId();
            if (myRoomIds.contains(targetRoomId)) {
                log.info("[CatalogMessageAdapter] 두 유저가 모두 참여 중인 기존 방 발견 - 방ID: {}", targetRoomId);
                return Optional.of(targetRoomId);
            }
        }

        //2차 비교: 상대방이 나간 경우 확인
        //로그인 유저가 참여한 방 중 대상자가 보낸 메시지 있는 거 확인
        log.info("[CatalogMessageAdapter] 동시 참여 중인 방 없음. 과거 메시지 이력 역추적 시작...");
        for (ChatRoomMemberJpaEntity myMeta : myMemberships) {
            Long roomId = myMeta.getRoomId().getId();

            //해당 채팅방에 속하면서 상대방 메시지가 존재하는지 확인
            boolean hasMessageFromTarget = springDataMessageRepository.existsByRoomId_IdAndSenderId_Id(roomId, targetUserId);

            if (hasMessageFromTarget) {
                log.info("[CatalogMessageAdapter] 과거 메시지 송수신 이력이 있는 기존 방 발견 - 방ID: {}", roomId);
                return Optional.of(roomId);
            }
        }
        //상대방이 보낸 메시지 흔적이 없다면 텅 빈 값 반환
        log.info("[CatalogMessageAdapter] 로그인 유저 채팅방에 상대방 메시지 내역 없음. 채팅방 신설 시작.");
        return Optional.empty();
    }
    //채팅방 생성 시점에 시간 주입
    @Override
    public void saveChatRoom(ChatRoomJpaEntity room) {
        room.changeCreatedAt(LocalDateTime.now());
        springDataChatRoomRepository.save(room);
        log.info("[CatalogMessageAdapter] chat_room 테이블 행 추가 완료 - 채팅방생성시간: {}", room.getCreatedAt());
    }
    //멤버 저장
    @Override
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
    @Override
    @Transactional
    public void saveMessage(MessageJpaEntity newMessage) {
        log.info("[CatalogMessageAdapter] message 테이블에 저장 - 요청자ID: {}", newMessage.getSenderId().getId());

        springDataMessageRepository.save(newMessage);
    }
}
