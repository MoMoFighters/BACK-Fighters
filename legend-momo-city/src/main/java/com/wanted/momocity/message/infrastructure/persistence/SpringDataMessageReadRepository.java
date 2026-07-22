package com.wanted.momocity.message.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface SpringDataMessageReadRepository extends JpaRepository<MessageReadJpaEntity, Long> {

    //채팅방 목록 안읽음 개수
    Long countByRoomId_IdAndUserId_IdAndIsMsgReadFalse(Long roomId, Long userId);

    //메시지 읽음 처리
    @Query("select mr from MessageReadJpaEntity mr " +
            "join fetch mr.roomId r " +
            "join fetch mr.messageId m " +
            "join fetch mr.userId u " +
            "where r.id = :roomId and u.id = :userId and mr.isMsgRead = false")
    List<MessageReadJpaEntity> findByRoomId_IdAndUserId_IdAndIsMsgReadFalse(@Param("roomId") Long roomId,
                                                                            @Param("userId") Long userId);
    @Modifying(clearAutomatically = true) // 🌟 필수: 업데이트 후 영속성 컨텍스트를 깨끗이 비워 정합성 유지
    @Query("update MessageReadJpaEntity mr " +
            "set mr.isMsgRead = true, mr.isNotiRead = true " +
            "where mr.roomId.id = :roomId " +
            "and mr.userId.id = :userId " +
            "and mr.isMsgRead = false")
    int bulkUpdateReadStatus(@Param("roomId") Long roomId, @Param("userId") Long userId);

    //채팅방 가가기: 읽음 여부 삭제
    @Modifying(clearAutomatically = true)
    @Query("delete from MessageReadJpaEntity mr where mr.roomId.id = :roomId")
    void deleteByRoomId_Id(@Param("roomId") Long roomId);

    //하나의 메시지당 읽지 않은 멤버 수
    Long countByMessageId_IdAndIsMsgReadFalse(Long messageId);

    //메시지 내역 조회(말풍선 안읽음 수)
    // 1. 모든 메시지의 안 읽은 사람 수를 단 한 번에 가져오는 벌크 쿼리 (Object[] 형태로 [메시지ID, 카운트])
    @Query("select mr.messageId.id, count(mr) from MessageReadJpaEntity mr " +
            "where mr.messageId.id in :messageIds and mr.isMsgRead = false " +
            "group by mr.messageId.id")
    List<Object[]> countUnreadMembersByMessageIdsIn(@Param("messageIds") List<Long> messageIds);

    //채팅방 목록 조회 개선 보강(내가 안읽은 메시지 수)
    // 1. 참여한 모든 방의 안읽은 메시지 수를 한방에 카운트 (방 ID별로 COUNT)
    @Query("SELECT m.roomId.id, COUNT(m) FROM MessageReadJpaEntity m " +
            "WHERE m.roomId.id IN :roomIds AND m.userId.id = :userId AND m.isMsgRead = false " + // (비즈니스 요구사항 조건에 맞춤)
            "GROUP BY m.roomId.id")
    List<Object[]> countUnreadMessagesByRoomIdsIn(@Param("roomIds") List<Long> roomIds, @Param("userId") Long userId);
}
