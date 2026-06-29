package com.wanted.momocity.notification.infrastructure.catalog;

import com.wanted.momocity.friend.user.UserWithFMJpaEntity;
import com.wanted.momocity.global.domain.common.exception.DomainRuleViolationException;
import com.wanted.momocity.message.infrastructure.persistence.MessageReadJpaEntity;
import com.wanted.momocity.notification.domain.model.Notification;
import com.wanted.momocity.notification.infrastructure.persistence.*;
import com.wanted.momocity.notification.domain.repository.NotificationRepository;
import com.wanted.momocity.user.infrastructure.persistence.SpringDataUserRepository;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class CatalogNotificationRepositoryAdapter implements NotificationRepository {

    private final SpringDataNotificationRepository springDataNotificationRepository;
    private final SpringDataUserRepository springDataUserRepository;
    private final NotificationSideUserRepository notificationSideUserRepository;
    private final NotificationSideChatRoomRepository notificationSideChatRoomRepository;
    private final NotificationSideMessageReadRepository notificationSideMessageReadRepository;
    private final EntityManager em;

    @Override
    @Transactional
    public Notification save(Notification notification) {
        log.info("[CatalogNotificationRepositoryAdapter] 알림 테이블 새 행 삽입 시도 - 대상자ID: {}, 타입: {}",
                notification.getUserId(), notification.getType());
        //도메인 모델에 담긴 userId로 연관된 UserJpaEntity 조회
        UserWithFMJpaEntity targetUser = notificationSideUserRepository.findById(notification.getUserId())
                .orElseThrow(() -> new DomainRuleViolationException("해당 유저를 찾을 수 없습니다. ID: " + notification.getUserId()));

        //도메인 모델 -> JPA 엔티티 변환
        NotificationJpaEntity jpaEntity = NotificationJpaEntity.toEntity(notification,targetUser);

        //실제 DB 영속화 후 다시 도메인 모델로 감싸서 변환
        return springDataNotificationRepository.save(jpaEntity).toDomain();
    }

    //친구 요청 철회
    @Override
    @Transactional
    public void deleteByRefIdAndUserId_IdAndType(Long refId, Long userId, String type) {
        log.info("[CatalogNotificationRepositoryAdapter] notification 테이블 행 영구 삭제 시도 - refId: {}, type: {}",
                refId, type);

        springDataNotificationRepository.deleteByRefIdAndUserId_IdAndType(refId, userId, type);

        log.info("[CatalogNotificationRepositoryAdapter] notification 테이블 행 삭제 완료");
    }

    //메시지 전송
    @Override
    public Optional<Notification> findByRefIdAndTypeAndUserId_Id(Long roomId, String type, Long senderId) {
        log.info("[CatalogNotificationRepositoryAdapter] 기존 메시지 알림 조회 시도 - 방ID: {}, 타입: {}, 발신자ID: {}", roomId, type, senderId);

        // 실제 Spring Data JPA 리포지토리를 호출하여 엔티티를 꺼내온 뒤, 도메인 모델로 복원하여 반환합니다.
        return springDataNotificationRepository.findByRefIdAndTypeAndUserId_Id(roomId, type, senderId)
                .map(NotificationJpaEntity::toDomain);
    }

    //알림 목록
    @Override
    public List<Object[]> findAllByUserId(Long userId) {
        log.info("[CatalogNotificationRepositoryAdapter] 한 방 쿼리로 알림 및 읽음 정보 통합 로드 시작 - 유저ID: {}", userId);
        return springDataNotificationRepository.findAllByUserId(userId);
    }

    //알림 목록 - 방 이름
    @Override
    public Optional<String> findRoomTitleById(Long roomId) {
        log.info("[CatalogNotificationRepositoryAdapter] 알림용 채팅방 타이틀 조회 - 방ID: {}", roomId);
        return notificationSideChatRoomRepository.findTitleById(roomId);
    }

    //(메인 페이지 종)안읽은 전체 알림 개수(메시지 알림 제외)
    @Override
    public long countUnreadGeneral(Long userId) {
        return springDataNotificationRepository.countUnreadGeneralNotifications(userId);
    }

    //(메인 페이지 종)안읽고 삭제하지 않은 메시지의 채팅방 개수
    @Override
    public long countUnreadMessageRooms(Long userId) {
        return notificationSideMessageReadRepository.countUnreadMessageRooms(userId);
    }

    //휴대폰 속 앱별 알림 개수(캘린더, 커뮤니티, 친구)
    @Override
    public long countByUserIdAndType(Long userId, String type) {
        return springDataNotificationRepository.countByUserIdAndType(userId, type);
    }

    //휴대폰 속 앱별 알림 개수(메시지)
    @Override
    public long countTotalUnreadMessages(Long userId) {
        return notificationSideMessageReadRepository.countByUserIdAndIsMsgReadFalse(userId);
    }

    //알림 읽기 - 요청온 알림이 notification 테이블에 존재하는지.
    @Override
    public List<NotificationJpaEntity> findAllByIdIn(List<Long> targetId) {
        return springDataNotificationRepository.findAllByIdIn(targetId);
    }

    //알림 읽기 - 메시지 알림의 refId(roomId)에 로그인 유저가 속하는지 검증
    @Override
    public List<MessageReadJpaEntity> findMessageReadsByRoomIdsAndUserId(List<Long> messageRoomIds, Long userId) {
        return notificationSideMessageReadRepository.findByRoomId_IdIn(messageRoomIds, userId);
    }

    //알림 읽기 - 일반 알림 읽음 상태 저장
    @Override
    @Transactional
    public void saveAll(List<NotificationJpaEntity> generalNotisToUpdate) {
        springDataNotificationRepository.saveAll(generalNotisToUpdate);
    }


    //알림 읽기 - 메시지 알림 읽음 상태 저장
    @Override
    @Transactional
    public void bulkMarkMessageNotificationsAsRead(List<Long> messageRoomIds, Long userId) {
        log.info("[Adapter] 메시지 알림 벌크 업데이트 쿼리 실행 - 방 개수: {}", messageRoomIds.size());
        notificationSideMessageReadRepository.bulkUpdateNotiReadTrue(messageRoomIds, userId);
    }


    //알림 읽음 상태 빠른 저장(읽음 개수 웹소켓)
    @Override
    public void fastSaveChanges() {
        log.info("[CatalogNotificationRepositoryAdapter] 벌크 연산 완료 후 영속성 컨텍스트 Flush 및 Clear 진행");
        em.flush(); // 쓰기 지연 저장소에 남아있을 수 있는 데이터 잔여물 방출
        em.clear(); // 🎯 핵심: 1차 캐시를 완전히 비워 웹소켓용 재조회 쿼리가 무조건 DB 최신 값을 읽도록 강제!
    }

    //알림 삭제 - 일반 알림
    @Override
    public void deleteAllInBatch(List<NotificationJpaEntity> generalNotisToDelete) {
        springDataNotificationRepository.deleteAllInBatch(generalNotisToDelete);
    }

    //알림 삭제 - 메시지 알림
    @Override
    public void bulkMarkMessageNotificationsAsDeleted(List<Long> messageRoomIds, Long userId) {
        notificationSideMessageReadRepository.bulkUpdateIsDeletedTrue(messageRoomIds, userId);
    }
}
