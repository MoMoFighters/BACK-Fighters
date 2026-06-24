package com.wanted.momocity.streak.domain.repository;

import com.wanted.momocity.streak.domain.model.Streak;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface StreakRepository {

    // 저장 (신규 생성 + 수정)
    Streak save (Streak streak);

    // 특정 날짜 잔디 조회 -> 오늘 잔디가 있는지 확인용
    Optional<Streak> findByUserIdAndStreakDate(Long userId, LocalDate streakDate);

    // 월간 잔디 조회 -> 메인 페이지 진입 시 한달치 잔디 조회
    List<Streak> findUserIdAndStreakDateBetween(
            Long userId, LocalDate startDate, LocalDate endDate
    );

}
