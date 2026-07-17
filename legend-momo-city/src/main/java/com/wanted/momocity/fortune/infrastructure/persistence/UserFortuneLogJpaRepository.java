package com.wanted.momocity.fortune.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.Optional;

// user_fortune_logs 테이블의 실제 Db 접근 담당
public interface UserFortuneLogJpaRepository extends JpaRepository<UserFortuneLogJpaEntity, Long> {

    // 사용장 ID와 날짜 모두 일치하는 운세 기록 한 건을 조회
    Optional<UserFortuneLogJpaEntity> findByUserIdAndDrawnDate(
            Long userId,
            LocalDate drawnDate
    );
}
