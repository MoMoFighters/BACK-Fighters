package com.wanted.momocity.chatbot.domain.repository;

/* comment.
    ChatbotDailyUsage 를 저장 및 조회하는 방법을 정의만 하는 도메인 인터페이스
    실제 구현은 infra 계층의 어댑터가 담당 예정
 */

import com.wanted.momocity.chatbot.domain.model.ChatbotDailyUsage;

import java.time.LocalDate;
import java.util.Optional;

public interface ChatbotDailyUsageRepository {

    // 유저 + 날짜로 오늘 사용 기록이 이미 있는지 조회하며 (find-or-create 패턴의 find 부분)
    Optional<ChatbotDailyUsage> findByUserIdAndUsageDate(Long userId, LocalDate usageDate);

    // 락 걸고 조회 (checkAndIncrease 전용, 동시성 제어)
    Optional<ChatbotDailyUsage> findByUserIdAndUsageDateForUpdate(Long userId, LocalDate usageDate);

    // 신규 생성 또는 기존 값 갱신 저장
    ChatbotDailyUsage save(ChatbotDailyUsage chatbotDailyUsage);

}
