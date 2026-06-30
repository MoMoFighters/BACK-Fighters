package com.wanted.momocity.message.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface SpringDataMessageRepository extends JpaRepository<MessageJpaEntity, Long> {
    //채팅방 목록
    //읽지 않은 메시지 개수 카운트
    //방 번호가 일치하고 보낸 사람이 로그인 유저가 아니며 아직 안 읽은 메시지 수 집계
//    Long countByRoomId_IdAndSenderId_IdNotAndIsMsgReadFalse(Long roomId, Long userId);

    //해당 방의 가장 최근 메시지 단 한 건만 가져오기
    // 🎯 [패치 조인 적용] 방 ID로 가장 최신 메시지 한 건을 긁어올 때,
    // 발신자 정보(senderId)까지 한방에 패치 조인으로 묶어서 가져옵니다.
    @Query("select m from MessageJpaEntity m " +
            "join fetch m.senderId " +
            "where m.roomId.id = :roomId " +
            "order by m.id desc limit 1")
    Optional<MessageJpaEntity> findFirstByRoomId_IdOrderByIdDesc(@Param("roomId") Long roomId);

    //상대방이 나가서 로그인 유저 혼자 남았을 때 메시지 내역 중 상대방이 보낸 거 최신 거 가져오기(상대방Id 가져오기 위함)
    // 🎯 [패치 조인 적용] 로그인 유저를 제외한 마지막 메시지를 조회해 올 때,
    // 나간 상대방의 정보(senderId)까지 한방에 패치 조인으로 묶어서 가져옵니다.
    @Query("select m from MessageJpaEntity m " +
            "join fetch m.senderId " +
            "where m.roomId.id = :roomId and m.senderId.id != :userId " +
            "order by m.id desc limit 1")
    Optional<MessageJpaEntity> findFirstByRoomId_IdAndSenderId_IdNotOrderByIdDesc(@Param("roomId") Long roomId,
                                                                                  @Param("userId") Long userId);

    //채팅방 조회 및 개설
    //상대방이 보낸 내역 있는 채팅방 여부
    boolean existsByRoomId_IdAndSenderId_Id(Long roomId, Long senderId);

//    //메시지 읽음 처리
//    List<MessageJpaEntity> findByRoomId_IdAndSenderId_IdAndIsReadFalse(Long roomId, Long id);

    //메시지 내역 조회
    //lastMessageId 없을 때(최조 진입)
    @Query("select m from MessageJpaEntity m " +
            "join fetch m.senderId " +
            "where m.roomId.id = :roomId " +
            "  and m.createdAt >= :timeline " +
            "order by m.id desc limit 20")
    List<MessageJpaEntity> findTop20ByRoomId_IdAndCreatedAtGreaterThanEqualOrderByIdDesc(@Param("roomId") Long roomId,
                                                                                         @Param("timeline") LocalDateTime timeline);
    //스크롤 시 lastMessageId보다 작은 과거 데이터(최신) 20개
    @Query("select m from MessageJpaEntity m " +
            "join fetch m.senderId " +
            "where m.roomId.id = :roomId " +
            "  and m.id < :lastMessageId " +
            "  and m.createdAt >= :timeline " +
            "order by m.id desc limit 20")
    List<MessageJpaEntity> findTop20ByRoomId_IdAndIdLessThanAndCreatedAtGreaterThanEqualOrderByIdDesc(@Param("roomId") Long roomId,
                                                                                                      @Param("lastMessageId") Long lastMessageId,
                                                                                                      @Param("timeline") LocalDateTime timeline);

    //채팅방 폭파 시 메시지 삭제
    void deleteByRoomId_Id(Long aLong);

    //메시지 내역 조회: 마지막 메시지의 시간
    @Query("select m.createdAt from MessageJpaEntity m where m.id = :messageId")
    Optional<LocalDateTime> findCreatedAtById(@Param("messageId") Long messageId);}
