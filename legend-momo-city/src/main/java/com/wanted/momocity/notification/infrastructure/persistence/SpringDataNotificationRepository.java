package com.wanted.momocity.notification.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface SpringDataNotificationRepository extends JpaRepository<NotificationJpaEntity, Long> {

    //친구 요청 철회
    void deleteByRefIdAndUserId_IdAndType(Long refId, Long userId, String type);

    //메시지 전송
    Optional<NotificationJpaEntity> findByRefIdAndTypeAndUserId_Id(Long refId, String type, Long userId);

    //알림 목록
    @Query("SELECT n, mr FROM NotificationJpaEntity n " +
            "LEFT JOIN MessageReadJpaEntity mr ON n.refId = mr.roomId.id AND mr.userId = :userId AND mr.isDeleted = false " +
            "WHERE (n.type != 'MESSAGE' AND n.userId.id = :userId) " + // 일반 알림은 수신자가 나인 것
            "   OR (n.type = 'MESSAGE' AND n.userId.id != :userId)")  // 메시지 알림은 발신자가 내가 아닌 것 (즉, 남이 보낸 것)
    List<Object[]> findAllByUserId(@Param("userId") Long userId);
}
