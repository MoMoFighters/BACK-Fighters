package com.wanted.momocity.notification.infrastructure.catalog;

import com.wanted.momocity.friend.user.UserWithFMJpaEntity;
import com.wanted.momocity.global.domain.common.exception.DomainRuleViolationException;
import com.wanted.momocity.notification.domain.model.Notification;
import com.wanted.momocity.notification.infrastructure.persistence.NotificationJpaEntity;
import com.wanted.momocity.notification.domain.repository.NotificationRepository;
import com.wanted.momocity.notification.infrastructure.persistence.NotificationSideUserRepository;
import com.wanted.momocity.notification.infrastructure.persistence.SpringDataNotificationRepository;
import com.wanted.momocity.user.infrastructure.persistence.SpringDataUserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Repository
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class CatalogNotificationRepositoryAdapter implements NotificationRepository {

    private final SpringDataNotificationRepository springDataNotificationRepository;
    private final SpringDataUserRepository springDataUserRepository;
    private final NotificationSideUserRepository notificationSideUserRepository;

    @Override
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
    public void deleteByRefIdAndType(Long refId, String type) {
        log.info("[CatalogNotificationRepositoryAdapter] notification 테이블 행 영구 삭제 시도 - refId: {}, type: {}",
                refId, type);

        springDataNotificationRepository.deleteByRefIdAndType(refId, type);

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
}
