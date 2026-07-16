package com.wanted.momocity.chatbot.application.service;

import com.wanted.momocity.chatbot.application.usecase.ChatbotUsageUseCase;
import com.wanted.momocity.chatbot.domain.model.ChatbotDailyUsage;
import com.wanted.momocity.chatbot.domain.repository.ChatbotDailyUsageRepository;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
/* comment.
    챗봇 사용량 조회 그리고 한도 체크 및 증가를 처리하는 서비스이다. (증가는 고려중)
    checkAndIncrease 는 조회 ~ 증가 사이를 트랜젝션 + 락을 통해서
    동시 요청으로 하루 5회 제한이 뚫리는 것을 막는다. (추후 숫자가 연장될 수 있음)
 */

@Service
@RequiredArgsConstructor
public class ChatbotUsageService implements ChatbotUsageUseCase {

    private final ChatbotDailyUsageRepository chatbotDailyUsageRepository;

    // 오늘 기록 없으면(아직 0회) 임시 객체로 대체, 저장 안하고 값만 리턴한다.
    @Override
    @Transactional(readOnly = true)
    public UsageStatus getTodayUsage(Long userId) {
        ChatbotDailyUsage usage = chatbotDailyUsageRepository
                .findByUserIdAndUsageDate(userId, LocalDate.now())
                .orElseGet(() -> new ChatbotDailyUsage(userId, LocalDate.now()));

        return new UsageStatus(usage.getCallCount(), usage.getDailyLimit());
    }

    // findByUserIdAndUsageDate 로 락을 걸고 조회한다.
    // 도메인 규칙이 한도 체크까지 알아서 한다.
    @Override
    @Transactional
    public void checkAndIncrease(Long userId) {
        LocalDate today = LocalDate.now();
        ChatbotDailyUsage usage = chatbotDailyUsageRepository
                .findByUserIdAndUsageDateForUpdate(userId, today)
                .orElseGet(() -> new ChatbotDailyUsage(userId, today));

        usage.increaseCallCount();
        chatbotDailyUsageRepository.save(usage);
    }

}
