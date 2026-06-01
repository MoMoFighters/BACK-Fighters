package com.wanted.momocity.viewing.infrastructure.persistence;

import com.wanted.momocity.global.infrastructure.persistence.BaseTimeEntity;
import com.wanted.momocity.viewing.domain.model.LearningHistory;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

/*
* comment.
*  infrastructure.persistence
*  - DB 테이블과 1:1 매핑되는 JPA 클래스
*  - Domain Model (LearningHistory) 을 모르고, DB 컬럼 구조만 표현함
*  -
*  Domain Model 과 JpaEntity 를 분리하는 이유
*  - Domain Model -> 순수 비지니스 규칙 (JPA 모름)
*  - JpaEntity -> DB 매핑 전용 (비지니스 규칙 없음)
*  - 둘 사이 변환은 RepositoryAdapter 가 담당
* */

@Getter
@Entity
@Table(name = "learning_history")
@NoArgsConstructor
public class LearningHistoryJpaEntity extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name ="user_id", nullable = false)
    private Long userId;

    @Column(name = "lecture_id", nullable = false)
    private Long lectureId;

    @Column(name = "chapter_id", nullable = false)
    private Long chapterId;

    @Column(name = "watched_seconds", nullable = false)
    private int watchedSeconds;

    @Column(name = "is_completed", nullable = false)
    private boolean isCompleted;

    @Column(name = "last_position_sec", nullable = false)
    private int lastPositionSec;

    @Column(name = "progress_rate", nullable = false)
    private int progressRate;

    /*
    * comment.
    *  @Version : 낙관적 락 (Optimistic Lock) 핵심 필드
    *  [동작 방식]
    *  조회 시 version 값 합께 읽어옴 -> 저장 시 현재 version 과 DB version 비교
    *  -> 일치하면 저장 후 version +1 -> 실제 DB Lock 없이 충돌 감지 가능 (성능 유리)
    *  -> 불일치하면 OptimisticLockingFailureException 발생
    *  -
    *  5-10 초 주기로 saveProgress() 호출
    *  -> 같은 챕터 동시 요청 시 데이터 정합성 보장
    *  -> 실제 DB Lock 없이 충돌 감지 가능 (성능 유리)
    * */

    @Version
    @Column(name = "version", nullable = false)
    private Long version;

    /*
    * comment.
    *  @Version 이 하는 일
    *  1. 저장 시 자동으로 Version + 1
    *  2. 저장 시 WHERE version = ? 조건 추가
    *  -> UPDATE learning_history
    *     SET watched_seconds = ?, version = 1
    *     WHERE id = ? AND version = 0 <- 해당 조건 불일치시 Exception 발생
    * */

    // Domain Model -> JpaEntity 변환 (저장용)
    public static LearningHistoryJpaEntity from(LearningHistory domain) {
        LearningHistoryJpaEntity entity = new LearningHistoryJpaEntity();
        entity.id = domain.getId();
        entity.userId = domain.getUserId();
        entity.lectureId = domain.getLectureId();
        entity.chapterId = domain.getChapterId();
        entity.watchedSeconds = domain.getWatchedSeconds();
        entity.isCompleted = domain.isCompleted();
        entity.lastPositionSec = domain.getLastPositionSec();
        entity.progressRate = domain.getProgressRate();
        entity.version = domain.getVersion();
        return entity;
    }

    // JpaEntity -> Domain Model 변환 (조회용)
    public LearningHistory toDomain() {
        return LearningHistory.reconstitute(
                id, userId,lectureId, chapterId, watchedSeconds,
                isCompleted, lastPositionSec, progressRate, version
        );
    }

}
