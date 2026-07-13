package com.wanted.momocity.notification.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
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
    // 🎯 [최적화] 알림 목록 조회 시 발신자/수신자 유저 객체를 한방에 패치 조인!
    @Query("SELECT n, mr FROM NotificationJpaEntity n " +
            "JOIN FETCH n.userId u " + // 👈 유저 객체 미리 묶어오기 (N+1 방어)
            "LEFT JOIN MessageReadJpaEntity mr ON n.type = 'MESSAGE' AND n.refId = mr.roomId.id AND mr.userId.id = :userId AND mr.isDeleted = false " +
            "WHERE (n.type != 'MESSAGE' AND n.userId.id = :userId) " +
            "   OR (n.type = 'MESSAGE' AND n.userId.id != :userId)")
    List<Object[]> findAllByUserId(@Param("userId") Long userId);

    //메시지를 제외한 모든 알림 개수
    @Query("SELECT COUNT(n) FROM NotificationJpaEntity n " +
            "WHERE n.userId.id = :userId " +
            "AND n.type != 'MESSAGE' " +
            "AND n.isRead = false")
    long countUnreadGeneralNotifications(@Param("userId") Long userId);

    //휴대폰 속 앱별 알림 개수(캘린더, 커뮤니티, 친구)
    @Query("SELECT COUNT(n) FROM NotificationJpaEntity n WHERE n.userId.id = :userId AND n.type = :type AND n.isRead = false")
    long countByUserIdAndType(@Param("userId") Long userId, @Param("type") String type);

    //알림 읽기 - 요청온 알림이 notification 테이블에 존재하는지.
    // 🎯 [최적화 추가] 알림 읽기 처리 시 요청 타겟 알림들과 수신자 유저 정보를 한방에 긁어옵니다.
    @Query("SELECT n FROM NotificationJpaEntity n " +
            "JOIN FETCH n.userId u " + // 👈 N+1 원천 차단용 패치 조인
            "WHERE n.id IN :targetId")
    List<NotificationJpaEntity> findAllByIdIn(List<Long> targetId);

    @Query("SELECT n.type, COUNT(n) FROM NotificationJpaEntity n " +
            "WHERE n.userId.id = :userId " +
            "AND n.type IN ('CALENDAR', 'POST', 'FRIEND_REQUEST', 'STUDY_INVITE') " +
            "AND n.isRead = false " +
            "GROUP BY n.type")
    List<Object[]> countUnreadGroupByType(@Param("userId") Long userId);

    //친구 거절 알림 읽음 처리
    // 🎯 [추가] 1방의 JPQL UPDATE 쿼리로 알림 상태만 '읽음'으로 변경
    @Modifying
    @Query("update NotificationJpaEntity n set n.isRead = true " +
            "where n.refId = :refId and n.userId.id = :userId and n.type = :type and n.isRead = false")
    int bulkMarkAsReadByRefIdAndUserIdAndType(
            @Param("refId") Long refId,
            @Param("userId") Long userId,
            @Param("type") String type
    );

    //일반 알림 읽기 벌크 처리
    @Modifying(clearAutomatically = true) // 벌크 연산 후 영속성 컨텍스트 싱크 유지
    @Query("UPDATE NotificationJpaEntity n " +
            "SET n.isRead = true " +
            "WHERE n.id IN :notificationIds " +
            "  AND n.isRead = false")
    void bulkMarkGeneralNotificationsAsRead(@Param("notificationIds") List<Long> notificationIds);

    //일반 알림 삭제 벌크 처리
    @Modifying(clearAutomatically = true) // 🎯 벌크 연산 후 영속성 컨텍스트 클리어 필수
    @Query("DELETE FROM NotificationJpaEntity n WHERE n.id IN :notificationIds")
    void bulkDeleteGeneralNotifications(@Param("notificationIds") List<Long> notificationIds);
}
