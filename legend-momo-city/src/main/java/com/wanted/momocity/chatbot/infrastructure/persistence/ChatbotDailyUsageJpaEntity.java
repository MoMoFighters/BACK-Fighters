package com.wanted.momocity.chatbot.infrastructure.persistence;

import com.wanted.momocity.global.infrastructure.persistence.BaseTimeEntity;
import jakarta.persistence.Entity;

// created_at/updated_at 둘 다 있는 스키마라 BaseTimeEntity(자동 시간 채움) 상속
@Entity
public class ChatbotDailyUsageJpaEntity extends BaseTimeEntity {
}
