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
public class MembershipExpireScheduler {

    /*comment
    *  매일 스케줄러 돌면서 membershipUntil 끝나는 사용자들 멤버십 basic으로 내림 */

    private final SpringDataUserRepository springDataUserRepository;

    // 매일 자정 실행
    @Scheduled(cron = "0 0 0 * * *")
    @Transactional
    public void revertExpiredMemberships() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime threshold = now.minusDays(30);

        int count = springDataUserRepository.revertExpiredMemberships(threshold, now);
        log.info("[MembershipExpireScheduler] 멤버십 만료 처리 완료 - {}명 BASIC 전환", count);
    }
}

