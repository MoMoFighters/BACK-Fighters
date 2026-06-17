package com.wanted.momocity.streak.application.usecase;

import java.time.LocalDate;

/*
* comment.
*  잔디 누적 UseCase 인터페이스
*  -> ChapterCompleteEvent 구독 시 호출
*  -> daily_watched_seconds 누적 + level 재계산
* */

public interface StreakCommandUseCase {
    void accumulate(Long userId, LocalDate date, int watchedSeconds);
}
