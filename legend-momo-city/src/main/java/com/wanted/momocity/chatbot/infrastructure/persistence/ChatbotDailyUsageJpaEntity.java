package com.wanted.momocity.chatbot.infrastructure.persistence;

import com.wanted.momocity.global.infrastructure.persistence.BaseTimeEntity;

// created_at/updated_at 둘 다 있는 스키마라 BaseTimeEntity(자동 시간 채움) 상속
// DBA 최종 스키마 확정 후 필드 채우고 @Entity 다시 부착
public class ChatbotDailyUsageJpaEntity extends BaseTimeEntity {
}
