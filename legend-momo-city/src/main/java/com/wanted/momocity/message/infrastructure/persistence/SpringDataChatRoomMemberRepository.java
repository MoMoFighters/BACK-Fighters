package com.wanted.momocity.message.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SpringDataChatRoomMemberRepository extends JpaRepository<ChatRoomMemberJpaEntity, Long> {
    //로그인 유저가 들어있는 모든 채팅방 멤버 행 조회
    List<ChatRoomMemberJpaEntity> findByUserId_Id(Long userId);

    //특정 방에 속한 모든 멤버들 조회(상대방을 고르기 위함)
    List<ChatRoomMemberJpaEntity> findByRoomId_Id(Long roomId);

    //회원가입 완료 이벤트로 나와의 채팅 생성 시 나와의 채팅방 기존 존재 여부 확인
    boolean existsByUserId_Id(Long userId);

    //나와의 채팅방 찾기 위함
    Optional<ChatRoomMemberJpaEntity> findFirstRoomIdByUserId_Id(Long senderId);

    //메시지 전송(로그인한 유저가 해당 채팅방의 멤버인지 확인)
    boolean existsByRoomId_IdAndUserId_Id(Long roomId, Long userId);

//    //로그인한 유저가 참여하는 방중 가장 먼저 만들어진 방 찾기(나와의 채팅방)
//    Long findFirstRoomIdByUserId(Long userId);
}
