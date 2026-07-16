package com.wanted.momocity.study.infrastructure.persistence;

import com.wanted.momocity.global.infrastructure.persistence.BaseTimeEntity;
import com.wanted.momocity.study.domain.model.SoloSession;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/*
 * comment.
 *  DB 테이블(solo_session)과 1:1 매핑되는 JPA 클래스
 *  -> Domain Model (SoloSession) 을 모르고 DB 컬럼 구조만 표현
 *  -> 변환은 SoloSessionRepositoryAdapter 가 담당
 * */

@Getter
@Entity
@Table(name = "solo_session")
@NoArgsConstructor
public class SoloSessionJpaEntity extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private SoloSession.SoloSessionStatus status;

    @Column(name = "start_time", nullable = false)
    private LocalDateTime startTime;

    @Column(name = "end_time")
    private LocalDateTime endTime;

    @Column(name = "total_seconds", nullable = false)
    private int totalSeconds;
    
    @Column(name = "last_resumed_at")
    private LocalDateTime lastResumedAt;

    // Domain -> JpaEntity 변환 (저장용)
    public static SoloSessionJpaEntity from(SoloSession domain) {
        SoloSessionJpaEntity entity = new SoloSessionJpaEntity();
        entity.id = domain.getId();
        entity.userId = domain.getUserId();
        entity.status = domain.getStatus();
        entity.startTime = domain.getStartTime();
        entity.endTime = domain.getEndTime();
        entity.totalSeconds = domain.getTotalSeconds();
        entity.lastResumedAt = domain.getLastResumedAt();
        return entity;
    }

    // JpaEntity -> Domain 변환 (조회용)
    public SoloSession toDomain() {
        return SoloSession.reconstitute(
                id, userId, status, startTime, endTime, totalSeconds, lastResumedAt,
                getCreatedAt(), getUpdatedAt()
        );
    }


}
