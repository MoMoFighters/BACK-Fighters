package com.wanted.momocity.notification.infrastructure.persistence;

import com.wanted.momocity.message.infrastructure.persistence.ChatRoomJpaEntity;
import com.wanted.momocity.message.infrastructure.persistence.MessageReadJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface NotificationSideMessageReadRepository extends JpaRepository<MessageReadJpaEntity, Long> {

    //총 알림 개수(메인 페이지 종)
    //읽지 않고 삭제되지 않는 메시지가 있는 채팅방의 수 = 알림 목록에 있는 메시지 알림 수
    @Query("SELECT COUNT(DISTINCT mr.roomId.id) FROM MessageReadJpaEntity mr " +
            "WHERE mr.userId.id = :userId " +
            "AND mr.isNotiRead = false " +
            "AND mr.isDeleted = false")
    long countUnreadMessageRooms(@Param("userId") Long userId);

    //휴대폰 속 앱별 알림 개수(메시지) - isMsgRead가 false인 것.
    @Query("SELECT COUNT(mr) FROM MessageReadJpaEntity mr WHERE mr.userId.id = :userId AND mr.isMsgRead = false")
    long countByUserIdAndIsMsgReadFalse(@Param("userId") Long userId);

    //알림 읽기 - 메시지 알림 권한 확인
    @Query("SELECT mr FROM MessageReadJpaEntity mr WHERE mr.roomId.id IN :messageRoomIds")
    List<MessageReadJpaEntity> findByRoomId_IdIn(List<Long> messageRoomIds);

    //알림 읽기 - 메시지 알림의 refId(roomId)에 로그인 유저가 속하는지 검증
    @Modifying
    @Query("UPDATE MessageReadJpaEntity mr " +
            "SET mr.isNotiRead = true " +
            "WHERE mr.roomId.id IN :roomIds " +
            "  AND mr.userId.id = :userId " +
            "  AND mr.isNotiRead = false " +
            "  AND mr.isDeleted = false")
    void bulkUpdateNotiReadTrue(@Param("roomIds") List<Long> roomIds, @Param("userId") Long userId);

    // 알림 삭제 - 메시지 알림의 상응하는 방 데이터를 삭제(Soft Delete) 처리
    @Modifying
    @Query("UPDATE MessageReadJpaEntity mr " +
            "SET mr.isDeleted = true " + // 🎯 삭제 플래그 True 변경
            "WHERE mr.roomId.id IN :roomIds " +
            "  AND mr.userId.id = :userId " +
            "  AND mr.isDeleted = false") // 이미 삭제된 건 제외
    void bulkUpdateIsDeletedTrue(@Param("roomIds") List<Long> roomIds, @Param("userId") Long userId);
}
