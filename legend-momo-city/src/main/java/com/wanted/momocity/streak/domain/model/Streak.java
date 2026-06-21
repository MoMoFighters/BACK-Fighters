package com.wanted.momocity.streak.domain.model;

import java.time.LocalDate;

public class Streak {

    private Long id;
    private Long userId;
    private LocalDate streakDate;
    private int dailyWatchedSeconds;
    private StreakLevel level;

    // 신규 생성용
    public static Streak create(Long userId, LocalDate streakDate, int watchedSeconds) {
        Streak streak = new Streak();
        streak.userId = userId;
        streak.streakDate = streakDate;
        streak.dailyWatchedSeconds = watchedSeconds;
        streak.level = StreakLevel.from(watchedSeconds);
        return streak;
    }

    // DB 복원용
    public static Streak reconstitute(
            Long id, Long userId, LocalDate streakDate,
            int dailyWatchedSeconds, StreakLevel level
    ) {
        Streak streak = new Streak();
        streak.id = id;
        streak.userId = userId;
        streak.streakDate = streakDate;
        streak.dailyWatchedSeconds = dailyWatchedSeconds;
        streak.level = level;
        return streak;
    }

    // daily_watched_seconds 누적 -> level 재계산
    public void accumulate(int watchedSeconds) {
        // 음수 방어
        if (watchedSeconds <= 0) return;
        this.dailyWatchedSeconds += watchedSeconds;
        this.level = StreakLevel.from(this.dailyWatchedSeconds);
    }

    public Long getId() {return id;}
    public Long getUserId() {return userId;}
    public LocalDate getStreakDate() {return streakDate;}
    public int getDailyWatchedSeconds() {return dailyWatchedSeconds;}
    public StreakLevel getLevel() {return level;}

}
