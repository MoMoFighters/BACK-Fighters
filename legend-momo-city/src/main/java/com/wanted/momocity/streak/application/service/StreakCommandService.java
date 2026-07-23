package com.wanted.momocity.streak.application.service;

import com.wanted.momocity.streak.application.usecase.StreakCommandUseCase;
import com.wanted.momocity.streak.domain.model.Streak;
import com.wanted.momocity.streak.domain.model.StreakLevel;
import com.wanted.momocity.streak.domain.repository.StreakRepository;
import com.wanted.momocity.streak.infrastructure.metrics.StreakMetrics;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Caching;
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
    private final StreakMetrics streakMetrics;

    /*
    * comment.
    *  잔디 누적
    *  - 캐시 무효화
    *  accumulate() 호출 시 해당 userId + year + month 캐시 무효화
    *  -> 잔디 변경 시 캐시 즉시 삭제 -> 다음 조회 시 DB 에서 최신 데이터 반환
    * */

    @Override
    @Caching(evict = {
            // 월간 캐시 무효화
            @CacheEvict(
                    value = "streak",
                    key = "#userId + ':' + #date.year + ':' + #date.monthValue",
                    cacheManager = "redisCacheManager"
            ),
            // 연간 캐시 무효화
            @CacheEvict(
                    value = "streakYearly",
                    key = "#userId + ':yearly:' + #date.year",
                    cacheManager = "redisCacheManager"
            )
    })
    public void accumulate(Long userId, LocalDate date, int watchedSeconds) {

        // 오늘 잔디 조회 -> 있으면 누적, 없으면 신규 생성
        // Optional 상태에서 먼저 isNew를 판단하도록 순서 변경
        var existing = streakRepository.findByUserIdAndStreakDate(userId, date);
        boolean isNew = existing.isEmpty();
        Streak streak = existing.orElseGet(() -> Streak.create(userId, date, 0));

        if (isNew) {
            // 신규 생성 횟수 카운트
            streakMetrics.recordStreakCreated();
        }

        // 누적 전 레벨 저장
        StreakLevel beforeLevel = streak.getLevel();

        // daily_watched_seconds 누적 + level 재계산
        streak.accumulate(watchedSeconds);

        // 레벨업 여부 판단
        if (streak.getLevel().ordinal() > beforeLevel.ordinal()) {
            // 레벨업 횟수 카운트
            streakMetrics.recordStreakLevelUp();
        }

        streakRepository.save(streak);

        log.info("[Streak] 잔디 누적 완료 | userId={}, date={}, dailyWatchedSeconds={}, level={}",
                userId, date, streak.getDailyWatchedSeconds(), streak.getLevel());

    }
}
