package com.wanted.momocity.user.infrastructure.scheduler;

import com.wanted.momocity.user.infrastructure.persistence.SpringDataUserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Slf4j
@Component
@RequiredArgsConstructor
public class BanOverScheduler {
    // 매일매일 스케줄러 돌면서 정지 풀어줌

    private final SpringDataUserRepository springDataUserRepository;

    @Scheduled(cron = "0 0 0 * * *")
    @Transactional
    public void liftExpiredBans() {
        LocalDateTime now = LocalDateTime.now();
        int banOverCount = springDataUserRepository.banOver(now);
        log.info("[scheduler] 정지 만료 유저 ACTIVE 복귀 처리 완료 | time={} | count={}", now, banOverCount);
    }

}
