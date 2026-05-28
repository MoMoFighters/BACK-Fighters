package com.wanted.momocity.message.infrastructure.persistence;

import com.wanted.momocity.friend.infrastructure.persistence.FriendJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.awt.print.Pageable;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface SpringDataMessageRepository extends JpaRepository<MessageJpaEntity, Long> {
    //채팅방 목록
    //읽지 않은 메시지 개수 카운트
    //방 번호가 일치하고 보낸 사람이 로그인 유저가 아니며 아직 안 읽은 메시지 수 집계
    Long countByRoomId_IdAndSenderId_IdNotAndIsReadFalse(Long roomId, Long userId);
    //해당 방의 가장 최근 메시지 단 한 건만 가져오기
    Optional<MessageJpaEntity> findFirstByRoomId_IdOrderByIdDesc(Long roomId);
    //상대방이 나가서 로그인 유저 혼자 남았을 때 메시지 내역 중 상대방이 보낸 거 최신 거 가져오기(상대방Id 가져오기 위함)
    Optional<MessageJpaEntity> findFirstByRoomId_IdAndSenderId_IdNotOrderByIdDesc(Long roomId, Long userId);

    //채팅방 조회 및 개설
    //상대방이 보낸 내역 있는 채팅방 여부
    boolean existsByRoomId_IdAndSenderId_Id(Long roomId, Long senderId);

    //메시지 읽음 처리
    List<MessageJpaEntity> findByRoomId_IdAndSenderId_IdAndIsReadFalse(Long roomId, Long id);

    //메시지 내역 조회
    //lastMessageId 없을 때(최조 진입)
    List<MessageJpaEntity> findTop20ByRoomId_IdAndCreatedAtGreaterThanEqualOrderByIdDesc(Long roomId, LocalDateTime timeline);
    //스크롤 시 lastMessageId보다 작은 과거 데이터(최신) 20개
    List<MessageJpaEntity> findTop20ByRoomId_IdAndIdLessThanAndCreatedAtGreaterThanEqualOrderByIdDesc(Long roomId, Long lastMessageId, LocalDateTime timeline);

    //채팅방 폭파 시 메시지 삭제
    void deleteByRoomId_Id(Long aLong);

}
