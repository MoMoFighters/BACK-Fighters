package com.wanted.momocity.chatbot.infrastructure.adapter;

import com.wanted.momocity.chatbot.domain.repository.ChatbotQuestionLogRepository;
import com.wanted.momocity.chatbot.infrastructure.persistence.ChatbotQuestionLogJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class ChatbotQuestionLogRepositoryAdapter implements ChatbotQuestionLogRepository {

    private final ChatbotQuestionLogJpaRepository chatbotQuestionLogJpaRepository;
}
