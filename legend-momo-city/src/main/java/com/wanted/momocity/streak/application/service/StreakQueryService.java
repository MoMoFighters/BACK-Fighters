package com.wanted.momocity.streak.application.service;

import com.wanted.momocity.streak.application.usecase.StreakQueryUseCase;
import com.wanted.momocity.streak.domain.model.Streak;
import com.wanted.momocity.streak.domain.repository.StreakRepository;
import com.wanted.momocity.streak.presentation.api.response.StreakMonthlyResponse;
import com.wanted.momocity.streak.presentation.api.response.StreakResponse;
import com.wanted.momocity.streak.presentation.api.response.StreakYearlyResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

/*
* comment.
*  잔디 월간 조회 UseCase 구현체
*  -> 메인 페이지 진입 시 한달치 잔디 조회
*  -> 시청 기록 없는 날짜는 응답에 포함 안 함
* */

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class StreakQueryService implements StreakQueryUseCase {

    private final StreakRepository streakRepository;

    /*
    * comment.
    *  월간 잔디 조회
    *  @Cacheabel -> StreakRedisCacheConfig 의 streakCacheConfiguration 사용
    *  -> Jackson2JsonRedisSerialzer<StreakMonthlyResponse> 로 타입 명시
    *  - 캐시 키 : "streak::{userId}:{year}:{month}"
    *  - 무효화 : StreakCommandService.accumulate() 호출 시 해당 날짜 캐시 무효화
    * */

    @Override
    @Cacheable(
            value = "streak",
            key = "#userId + ':' + #startDate.year + ':' + #startDate.monthValue",
            cacheManager = "redisCacheManager"
    )
    public StreakMonthlyResponse getMonthlyStreak(Long userId, LocalDate startDate, LocalDate endDate) {

        // 단일 월 범위 검증
        // startDate 와 endDate 가 동일한 년-월에 속하는지 확인 -> 여러 달에 걸친 범위 방지
        if (startDate.getYear() != endDate.getYear() ||
                startDate.getMonthValue() != endDate.getMonthValue()) {
            throw new IllegalArgumentException("조회 범위는 동일한 년-월이어야 합니다.");
        }

        List<Streak> streaks = streakRepository
                .findUserIdAndStreakDateBetween(userId, startDate,endDate);

        List<StreakResponse> streakResponses = streaks.stream()
                .map(streak -> new StreakResponse(
                        streak.getStreakDate(),
                        streak.getLevel()
                ))
                .toList();

        log.info("[Streak] 월간 잔디 조회 완료 | userId={}, startDate={}, endDate={}, count={}",
                userId, startDate, endDate, streakResponses.size());

        return new StreakMonthlyResponse(streakResponses);
    }

    @Override
    public StreakYearlyResponse getYearlyStreak(Long userId, int year) {

        List<Streak> streaks = streakRepository.findByUserIdAndYear(userId, year);

        List<StreakResponse> streakResponses = streaks.stream()
                .map(streak -> new StreakResponse(
                        streak.getStreakDate(),
                        streak.getLevel()
                ))
                .toList();

        log.info("[Streak] 연간 잔디 조회 완료 | userId={}, year={}, count={}",
                userId, year, streakResponses.size());

        return new StreakYearlyResponse(streakResponses);

    }

    // 친구 잔디 월간 조회
    @Override
    public StreakMonthlyResponse getFriendMonthlyStreak(Long targetUserId, LocalDate startDate, LocalDate endDate) {
        return getMonthlyStreak(targetUserId, startDate, endDate);
    }
}
