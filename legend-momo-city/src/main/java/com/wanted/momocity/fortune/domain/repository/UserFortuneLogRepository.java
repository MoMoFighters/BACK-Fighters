package com.wanted.momocity.fortune.domain.repository;

import com.wanted.momocity.fortune.domain.model.UserFortuneLog;

import java.time.LocalDate;
import java.util.Optional;

// 사용자별 오늘의 운세 기록을 관리하는 도메인 Repository
public interface UserFortuneLogRepository {

    // 특정 사욧ㅇ자가 특정 날짜에 뽑은 운세 기록을 조회
    Optional<UserFortuneLog> findByUserIdAndDrawnDate(
            Long userId,
            LocalDate drawnDate
    );

    // 오늘 처음 뽑은 사용자의 운세 기록을 저장
    UserFortuneLog save(
            UserFortuneLog userFortuneLog
    );
}
