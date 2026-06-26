package com.wanted.momocity.user.infrastructure.scheduler;

import com.wanted.momocity.user.domain.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class UserHardDeleteScheduler {

    private final UserRepository userRepository;

    @Scheduled(cron = "0 0 0 1 * *") // 매월 1일 자정
//    @Scheduled(cron = "0 * * * * *") // 매 분마다 실행 - 테스트용
    public void hardDeleteExpiredUsers() {
        LocalDateTime threshold = LocalDateTime.now().minusMonths(3);
        List<Long> targetIds = userRepository.findDeletedUserIdsBefore(threshold);

        if (targetIds.isEmpty()) {
            log.info("[scheduler] 하드딜리트 대상 없음");
            return;
        }

        targetIds.forEach(userId -> {
            userRepository.deleteById(userId);
            log.info("[scheduler] 하드딜리트 완료 | userId={}", userId);
        });
        /*comment
        *  DBA와 얘기 해서 userId를 fk로 참조하고 있는 테이블에 대해 cascade 설정을 해서
        *  이벤트 발행 없이 user가 삭제되면 관련 데이터도 함께 삭제되도록 처리 함*/
    }
}