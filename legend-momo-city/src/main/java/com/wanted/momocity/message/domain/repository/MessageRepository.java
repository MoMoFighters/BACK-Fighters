package com.wanted.momocity.message.domain.repository;

import com.wanted.momocity.friend.enrollment.EnrollmentWithFMJpaEntity;
import com.wanted.momocity.friend.infrastructure.persistence.FriendJpaEntity;
import com.wanted.momocity.friend.user.UserWithFMJpaEntity;
import com.wanted.momocity.message.infrastructure.persistence.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

//포트 역할
public interface MessageRepository {

    //사용자 정보
    Optional<UserWithFMJpaEntity> findUserWithFMById(Long userId);

    //로그인한 사용자가 속한 모든 채팅방의 상대방 정보 및 마지막 메시지 내역 긁어오기
    List<ChatRoomQueryProjection> findChatRoomByUserId(Long userId);

    //안 읽은 카운트 포트 개발
    Long countUnreadMessage(Long roomId, Long userId);

    //수강 정보 찾기
    List<EnrollmentWithFMJpaEntity> findEnrollmentsByUserId(Long userId); //수강신청 조회

    //마지막 메시지 정보
    Optional<MessageJpaEntity> findLatestMessageExceptMe(Long roomId, Long loginUserId); //나간 상대방 식별 시 메시지 역추젹용

    //친구 상태 양방향 조회
    Optional<FriendJpaEntity> findFriendRelation(Long loginUserId, Long targetUserId);

    //안내 문구 정보
    Optional<LocalDateTime> findLatestAnnounceTime(Long roomId); //최신 안내문구 시간 조회용

    //방 존재 여부
    boolean existsRoomById(Long roomId);

    //방 멤버 여부
    boolean existsMemberByRoomIdAndUserId(Long roomId, Long userId);

    //과거 메시지 존재 여부
    boolean existsMessageByRoomIdAndSenderId(Long roomId, Long senderId);

    //유저가 참여중인 모든 방 멤버 정보 조회
    List<ChatRoomMemberJpaEntity> findChatRoomMembersByUserId(Long userId);

    //메시지 내역 페이징 조회 (커서 ID 유무에 따른 분기 처리 포함
    List<MessageJpaEntity> findMessageHistory(Long roomId, Long lastMessageId, LocalDateTime startTimeLine);

    //똑같이 사용자 정보 탐색인데 쿼리를 줄이기 위함
    UserWithFMJpaEntity getUserWithFMReferenceById(Long userId);

    //안읽은 메시지 내역
    void saveAllMessageRead(List<MessageReadJpaEntity> messageReads);

    //로그인 유저가 안읽은 메시지 내역 조회
    List<MessageReadJpaEntity> findUnreadMessages(Long roomId, Long userId);

    //채팅방 나가기: 모든 메시지 삭제
    void deleteMessagesByRoomId(Long roomId);

    //채팅방 나가기: 멤버 삭제
    void deleteChatRoomMember(ChatRoomMemberJpaEntity member);

    //채팅방 나가기: 채팅방 삭제
    void deleteChatRoom(ChatRoomJpaEntity room);

    //채팅방 개설 시 기존 채팅방 존재 여부 확인
    Optional<Long> findExistingRoom(Long userId, Long targetUserId);
    //신규 방 보관용 포트
    void saveChatRoom(ChatRoomJpaEntity room);
    //신규 멤버 참여 보관용 포트
    void saveChatRoomMember(ChatRoomMemberJpaEntity member);

    //채팅방 찾기
    Optional<ChatRoomJpaEntity> findChatRoomById(Long roomId);

    //멤버 찾기
    List<ChatRoomMemberJpaEntity> findMembersByRoomId(Long roomId);

    //새로운 메시지 저장
    void saveMessage(MessageJpaEntity newMessage);

    //안내 문구 내역 조회
    List<MessageAnnounceJpaEntity> findAnnounceHistory(Long roomId, LocalDateTime startTimeLine, LocalDateTime endTimeLine);

    //특정 메시지를 안읽은 사람 수
    Long countUnreadMembersForMessage(Long messageId);

    // 회원가입 직후 방 존재 여부 확인용
    boolean existsChatRoomMemberByUserId(Long userId);

    //나와의 채팅방 인식: 첫 번째 채팅방 조회
    Optional<Long> findFirstRoomIdByUserId(Long userId);

    //채팅방 나가기: 안내 문구 저장
    void saveLeaveAnnounce(ChatRoomJpaEntity roomId, UserWithFMJpaEntity leaveUserId, String leaveMessage);

    //메시지 내역 조회(마지막 메시지의 시간)
    Optional<LocalDateTime> findLatestMessageTimeById(Long messageId);

    //메시지 전송 웹소켓
    void fastSaveChanges();

    //채팅방 재입장: 안내 문구 저장(일대일 채팅방 나갔다가 재입장함)
    void saveEnterAnnounce(ChatRoomJpaEntity chatRoom, UserWithFMJpaEntity enterUser, String enterMessage);

    //친구 삭제 핸들러 서비스 - 상대가 나간 채팅방 안내 문구 검증
    boolean existsAnnounceByRoomIdAndTargetId(ChatRoomJpaEntity room, UserWithFMJpaEntity tagetUser);

    //채팅방 목록 조회 - 상대가 나간 채팅방 안내 문구 검증
    Optional<UserWithFMJpaEntity> findLatestAnnounceUser(Long roomId);

    //채팅방 이름 변경 안내 문구
    void saveRenameAnnounce(ChatRoomJpaEntity chatRoom, UserWithFMJpaEntity loginUser, String announceContent, LocalDateTime updatedAt);

    //다대다 채팅방 멤버 초대
    void saveInviteChatRoomMember(ChatRoomJpaEntity room, UserWithFMJpaEntity invitedUser, LocalDateTime joinedAt);

    //다대다 채팅방 멤버 초대 안내 문구
    void saveInviteAnnounce(ChatRoomJpaEntity chatRoom, UserWithFMJpaEntity loginUser, String inviteMessage, LocalDateTime createdAt);

    //친구 삭제 후 채팅방 나가기 버그 수정
    Optional<Long> findOneToOneChatRoomIdBetween(Long userId, Long targetUserId);
    //친구 삭제 후 채팅방 나가기 버그 수정
    Optional<ChatRoomMemberJpaEntity> findMemberByRoomIdAndUserId(Long foundRoomId, Long userId);

    //채팅방 목록 조회 개선 보강
    List<ChatRoomMemberJpaEntity> findByRoomId_IdIn(List<Long> allRoomIds);
    //채팅방 목록 조회 개선 보강
    List<Object[]> findLatestAnnounceTimeByRoomIdsIn(List<Long> allRoomIds);
    //채팅방 목록 조회 개선 보강
    List<EnrollmentWithFMJpaEntity> findByUserId_IdIn(List<Long> longs);

    //채팅방 조회 및 개설 개선 보강
    List<ChatRoomMemberJpaEntity> findOnePersonRoomsByUserId(Long targetUserId, Long loginUserId);
}
