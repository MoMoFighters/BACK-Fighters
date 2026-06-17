package com.wanted.momocity.streak.infrastructure.persistence;

import com.wanted.momocity.streak.domain.model.Streak;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

/*
* comment.
*  DB 테이블과 1:1 매핑되는 JPA 클래스
*  -> Domain Model (Streak) 을 모르고 DB 컬럼 구조만 표현
*  -> 변환은 StreakRepositoryAdapter 가 담당
* */

@Getter
@Entity
@Table(
        name = "streak",
        // (user_id, streak_date) UNIQUE 제약
        // -> 같은 날짜에 중복 생성 방지
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"user_id", "streak_date"})
        }
)

@NoArgsConstructor
public class StreakJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "streak_date", nullable = false)
    private LocalDate streakDate;

    @Column(name = "daily_watched_seconds", nullable = false)
    private int dailyWatchedSeconds;

    @Column(name = "level", nullable = false)
    private int level;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    // from() : Domain Model -> JpaEntity 변환 (저장용)
    public static StreakJpaEntity from(Streak domain) {
        StreakJpaEntity entity = new StreakJpaEntity();
        entity.id = domain.getId();
        entity.userId = domain.getUserId();
        entity.streakDate = domain.getStreakDate();
        entity.dailyWatchedSeconds = domain.getDailyWatchedSeconds();
        entity.level = domain.getLevel();
        entity.createdAt = LocalDateTime.now();
        return entity;
    }

    // toDomain() : JpaEntity -> DomainModel 병환 (조회용)
    public Streak toDomain() {
        return Streak.reconstitute(
                id, userId, streakDate, dailyWatchedSeconds, level
        );
    }

}
