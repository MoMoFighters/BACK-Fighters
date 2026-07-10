package com.wanted.momocity.chatbot.infrastructure.persistence;

import jakarta.persistence.Entity;

// 스키마에 updated_at 컬럼이 없어서 BaseTimeEntity 상속 불가 — created_at만 자체 @CreatedDate 필드로 채워야 함
@Entity
public class ChatbotQuestionLogJpaEntity {
}
