package com.wanted.momocity.chatbot.infrastructure.adapter;

import com.wanted.momocity.chatbot.domain.repository.ChatbotDailyUsageRepository;
import com.wanted.momocity.chatbot.infrastructure.persistence.ChatbotDailyUsageJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

// ChatbotQuestionLogRepositoryAdapter는 있는데 이 어댑터는 빠져있어서 새로 추가
@Repository
@RequiredArgsConstructor
public class ChatbotDailyUsageRepositoryAdapter implements ChatbotDailyUsageRepository {

    private final ChatbotDailyUsageJpaRepository chatbotDailyUsageJpaRepository;
}
