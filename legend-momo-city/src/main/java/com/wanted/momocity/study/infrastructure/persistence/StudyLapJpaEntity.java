package com.wanted.momocity.study.infrastructure.persistence;

import com.wanted.momocity.global.infrastructure.persistence.BaseTimeEntity;
import com.wanted.momocity.study.domain.model.StudyLap;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/*
 * comment.
 *  DB 테이블(study_lap)과 1:1 매핑되는 JPA 클래스
 *  -> Domain Model (StudyLap) 을 모르고 DB 컬럼 구조만 표현
 *  -> 변환은 StudyLapRepositoryAdapter 가 담당
 * */

@Getter
@Entity
@Table(name = "study_lap")
@NoArgsConstructor
public class StudyLapJpaEntity extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "room_id")
    private Long roomId;

    @Column(name = "session_id", nullable = false)
    private Long sessionId;

    @Column(name = "started_at", nullable = false)
    private LocalDateTime startedAt;

    @Column(name = "ended_at")
    private LocalDateTime endedAt;

    @Column(name = "seconds")
    private Integer seconds;

    // Domain -> JpaEntity 변환 (저장용)
    public static StudyLapJpaEntity from(StudyLap domain) {
        StudyLapJpaEntity entity = new StudyLapJpaEntity();
        entity.id = domain.getId();
        entity.userId = domain.getUserId();
        entity.roomId = domain.getRoomId();
        entity.sessionId = domain.getSessionId();
        entity.startedAt = domain.getStartedAt();
        entity.endedAt = domain.getEndedAt();
        entity.seconds = domain.getSeconds();
        return entity;
    }

    // JpaEntity -> Domain 변환 (조회용)
    public StudyLap toDomain() {
        return StudyLap.reconstitute(
                id, userId, roomId, sessionId, startedAt, endedAt, seconds,
                getCreatedAt(), getUpdatedAt()
        );
    }

}
