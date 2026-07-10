package com.wanted.momocity.chatbot.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

public interface ChatbotQuestionLogJpaRepository extends JpaRepository<ChatbotQuestionLogJpaEntity, Long> {
}
