package com.wanted.momocity.streak.application.service;

import com.wanted.momocity.streak.application.usecase.StreakCommandUseCase;
import com.wanted.momocity.streak.domain.model.Streak;
import com.wanted.momocity.streak.domain.repository.StreakRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

/*
* comment.
*  잔디 누적 UseCase 구현체
*  -> ChapterCompletedEvent 구독 시 호출
*  -> 오늘 잔디가 있으면 누적, 없으면 신규 생성
*  -
*  - (user_id, streak_date) UNIQUE 제약으로 날짜별 1개 보장
*  - accumulate() 로 daily_watched_seconds 누적 + level 재계산
* */

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class StreakCommandService implements StreakCommandUseCase {

    private final StreakRepository streakRepository;

    @Override
    public void accumulate(Long userId, LocalDate date, int watchedSeconds) {

        // 오늘 잔디 조회 -> 있으면 누적, 없으면 신규 생성
        Streak streak = streakRepository
                .findByUserIdAndStreakDate(userId, date)
                .orElse(Streak.create(userId, date, 0));

        // daily_watched_seconds 누적 + level 재계산
        streak.accumulate(watchedSeconds);

        streakRepository.save(streak);

        log.info("[Streak] 잔디 누적 완료 | userId={}, date={}, dailyWatchedSeconds={}, level={}",
                userId, date, streak.getDailyWatchedSeconds(), streak.getLevel());

    }
}
