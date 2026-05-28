package com.wanted.momocity.notification.infrastructure.persistence;

import com.wanted.momocity.friend.user.UserWithFMJpaEntity;
import com.wanted.momocity.notification.domain.model.Notification;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "notification")
@NoArgsConstructor
@Getter
public class NotificationJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private UserWithFMJpaEntity userId;

    @Column(name = "type")
    private String type;

    @Column(name = "ref_id")
    private Long refId;

    @Column(name = "message")
    private String message;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    //추후
//    @Column(name = "is_read")
//    private boolean isRead;

    //알림 행 추가(순수 도메인 모델을 JPA 엔티티로 맵핑해주는 부분
    public static NotificationJpaEntity toEntity(Notification domain, UserWithFMJpaEntity userJpaEntity) {
        NotificationJpaEntity entity = new NotificationJpaEntity();
        entity.id = domain.getId();
        entity.userId = userJpaEntity;
        entity.type = domain.getType(); // 타입 고정
        entity.refId = domain.getRefId();
        entity.message = domain.getMessage();
        //추후 주석 해제
//        entity.isRead = domain.isRead;
        entity.createdAt = LocalDateTime.now();
        return entity;
    }

    //메시지 전송 - 기존 행 발견 시 날짜를 메시지 전송 시간으로 업데이트, 안읽음 처리
    public void updateMessageNotification(String message, LocalDateTime createdAt) {
        this.message = message;
        this.createdAt = createdAt;
        //추후 isRead 생기면 주석 해제
//        this.isRead = false;
    }

    //JPA 엔티티를 순수 도메인 모델로 복원해주는 기계
    public Notification toDomain() {
        return new Notification(
                this.id,
                this.userId.getId(),
                this.type,
                this.refId,
                this.message
        );
    }

}
