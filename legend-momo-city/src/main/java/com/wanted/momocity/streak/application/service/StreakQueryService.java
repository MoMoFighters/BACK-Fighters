package com.wanted.momocity.streak.application.service;

import com.wanted.momocity.streak.application.usecase.StreakQueryUseCase;
import com.wanted.momocity.streak.domain.model.Streak;
import com.wanted.momocity.streak.domain.repository.StreakRepository;
import com.wanted.momocity.streak.presentation.api.response.StreakMonthlyResponse;
import com.wanted.momocity.streak.presentation.api.response.StreakResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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

    @Override
    public StreakMonthlyResponse getMonthlyStreak(Long userId, LocalDate startDate, LocalDate endDate) {
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

        return new StreakMonthlyResponse(startDate.getYear(), startDate.getMonthValue(), streakResponses);
    }
}
