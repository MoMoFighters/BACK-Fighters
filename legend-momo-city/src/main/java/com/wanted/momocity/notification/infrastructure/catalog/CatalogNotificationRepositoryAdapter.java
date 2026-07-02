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
        // 🎯 [기존 병목 제거] findById 단건 SELECT 쿼리 삭제 후 프록시 참조로 대체! (SELECT 1방 절약)
        UserWithFMJpaEntity targetUser = getUserReference(notification.getUserId());

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
    @Transactional
    public void deleteAllInBatch(List<NotificationJpaEntity> generalNotisToDelete) {
        if (generalNotisToDelete == null || generalNotisToDelete.isEmpty()) {
            return;
        }

        // 🎯 서비스에서 넘어온 엔티티 뭉치에서 순수 고유 Long ID 목록만 쏙 추출합니다.
        List<Long> notificationIds = generalNotisToDelete.stream()
                .map(NotificationJpaEntity::getId)
                .toList();

        log.info("[Adapter] 일반 알림 벌크 삭제 쿼리 실행 - 대상 개수: {}건", notificationIds.size());

        // 🎯 새로 만든 Spring Data JPA의 벌크 DELETE 메서드로 최종 토스!
        springDataNotificationRepository.bulkDeleteGeneralNotifications(notificationIds);    }

    //알림 삭제 - 메시지 알림
    @Override
    @Transactional
    public void bulkMarkMessageNotificationsAsDeleted(List<Long> messageRoomIds, Long userId) {
        notificationSideMessageReadRepository.bulkUpdateIsDeletedTrue(messageRoomIds, userId);
    }

    //채팅방 이름 조회
    @Override
    public List<Object[]> findRoomTitlesByIdsIn(List<Long> roomIds) {
        if (roomIds == null || roomIds.isEmpty()) {
            return List.of();
        }
        log.info("[CatalogNotificationRepositoryAdapter] {}개의 채팅방 타이틀 일괄 조회(IN 절)", roomIds.size());
        return notificationSideChatRoomRepository.findTitlesByRoomIdsIn(roomIds);
    }

    //개선 - 앱별 알림 통합해서 가져오기
    @Override
    public List<Object[]> countUnreadGroupByType(Long userId) {
        log.info("[CatalogNotificationRepositoryAdapter] 앱별 안읽은 알림 카운트 일괄 조회(GROUP BY) - 유저ID: {}", userId);
        return springDataNotificationRepository.countUnreadGroupByType(userId);
    }

    @Override
    public UserWithFMJpaEntity getUserReference(Long userId) {
        // 🎯 [최적화] 실제 DB를 조회하지 않고 가짜 프록시 객체만 즉시 반환하여 연관관계 매핑용 키값으로만 사용
        return em.getReference(UserWithFMJpaEntity.class, userId);
    }

    //친구 거절 알림 읽음 처리
    @Override
    @Transactional // 데이터 쓰기 작업이므로 필수
    public void bulkMarkAsReadByRefIdAndUserIdAndType(Long refId, Long userId, String type) {
        log.info("[CatalogNotificationRepositoryAdapter] 친구 거절 알림 벌크 읽음 처리 시도 - refId(관계ID): {}, 거절자ID: {}", refId, userId);

        springDataNotificationRepository.bulkMarkAsReadByRefIdAndUserIdAndType(refId, userId, type);

        // 🎯 벌크 쿼리 실행 후 영속성 컨텍스트를 비워 웹소켓 재조회 시 DB 최신 데이터가 반영되도록 강제
        em.flush();
        em.clear();
    }

    //일반 알림 벌크 읽음
    @Override
    @Transactional
    public void bulkMarkGeneralNotificationsAsRead(List<NotificationJpaEntity> generalNotisToUpdate) {
        if (generalNotisToUpdate == null || generalNotisToUpdate.isEmpty()) {
            return;
        }

        // 🎯 서비스가 넘겨준 엔티티 리스트에서 순수 Long ID만 쏙 뽑아서 변환합니다.
        List<Long> notificationIds = generalNotisToUpdate.stream()
                .map(NotificationJpaEntity::getId)
                .toList();

        log.info("[Adapter] 일반 알림 벌크 업데이트 쿼리 실행 - 대상 개수: {}건", notificationIds.size());

        // 🎯 ID 리스트를 원하는 Spring Data JPA의 벌크 메서드로 최종 전달!
        springDataNotificationRepository.bulkMarkGeneralNotificationsAsRead(notificationIds);
    }
}
