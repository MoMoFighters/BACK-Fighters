package com.wanted.momocity.streak.domain.model;

import java.time.LocalDate;

public class Streak {

    private Long id;
    private Long userId;
    private LocalDate streakDate;
    private int dailyWatchedSeconds;
    private int level;

    // 신규 생성용
    public static Streak create(Long userId, LocalDate streakDate, int watchedSeconds) {
        Streak streak = new Streak();
        streak.userId = userId;
        streak.streakDate = streakDate;
        streak.dailyWatchedSeconds = watchedSeconds;
        streak.level = calculateLevel(watchedSeconds);
        return streak;
    }

    // DB 복원용
    public static Streak reconstitute(
            Long id, Long userId, LocalDate streakDate,
            int dailyWatchedSeconds, int level
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
        this.level = calculateLevel(this.dailyWatchedSeconds);
    }

    // dailyWatchedSeconds 기준으로 레벨 계산
    private static int calculateLevel(int seconds) {
        if (seconds <= 0) return 0;
        if (seconds <= 600) return 1;
        if (seconds <= 1800) return 2;
        if (seconds <= 3600) return 3;
        return 4;
    }

    public Long getId() {return id;}
    public Long getUserId() {return userId;}
    public LocalDate getStreakDate() {return streakDate;}
    public int getDailyWatchedSeconds() {return dailyWatchedSeconds;}
    public int getLevel() {return level;}

}
