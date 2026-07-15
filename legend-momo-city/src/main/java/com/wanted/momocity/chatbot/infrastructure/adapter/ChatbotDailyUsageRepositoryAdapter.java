package com.wanted.momocity.chatbot.infrastructure.adapter;

import com.wanted.momocity.chatbot.domain.model.ChatbotDailyUsage;
import com.wanted.momocity.chatbot.domain.repository.ChatbotDailyUsageRepository;
import com.wanted.momocity.chatbot.infrastructure.persistence.ChatbotDailyUsageJpaEntity;
import com.wanted.momocity.chatbot.infrastructure.persistence.ChatbotDailyUsageJpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Optional;

/* comment.
    도메인 포트 ChatbotDailyUsageRepository 를 실제로 구현
    도메인 모델과 JPA 엔티티 변환까지 여기서 전부 책임진다.
 */

/* comment.
    같은 BC 안인데도 Port(인터페이스) / Adapter(구현체)를 나누는 이유.

    1. 도메인 모델이 JPA를 몰라야 한다 — 비즈니스 규칙(하루 5회 제한 등)이랑
       영속성 기술(@Entity, @Column)이 섞이면 안 됨.
    2. 나중에 JPA 말고 다른 기술로 바꿔도 도메인/Service는 안 건드리고
       이 Adapter 파일만 갈아끼우면 된다.
    3. Service 테스트할 때 진짜 DB 없이도, 이 인터페이스를 가짜로 만들어서
       테스트 가능해진다.
 */
@Repository
public class ChatbotDailyUsageRepositoryAdapter implements ChatbotDailyUsageRepository {

    private final ChatbotDailyUsageJpaRepository chatbotDailyUsageJpaRepository;

    public ChatbotDailyUsageRepositoryAdapter(ChatbotDailyUsageJpaRepository chatbotDailyUsageJpaRepository) {
        this.chatbotDailyUsageJpaRepository = chatbotDailyUsageJpaRepository;
    }

    @Override
    // JPA 엔티티로 조회 후 도메인 모델로 변환해서 반환 (도메인은 JPA를 절대 모름)
    public Optional<ChatbotDailyUsage> findByUserIdAndUsageDate(Long userId, LocalDate usageDate) {
        return chatbotDailyUsageJpaRepository.findByUserIdAndUsageDate(userId, usageDate)
                .map(this::toDomain);
    }

    @Override
    // id가 없으면 오늘 첫 호출 → 신규 insert, 있으면 기존 행 찾아서 값만 갱신
    public ChatbotDailyUsage save(ChatbotDailyUsage chatbotDailyUsage) {
        ChatbotDailyUsageJpaEntity entity = (chatbotDailyUsage.getId() == null)
                ? new ChatbotDailyUsageJpaEntity(chatbotDailyUsage.getUserId(), chatbotDailyUsage.getUsageDate())
                : chatbotDailyUsageJpaRepository.findById(chatbotDailyUsage.getId())
                  .orElseThrow(() -> new IllegalStateException("수정 대상 사용량 기록이 없습니다. id=" + chatbotDailyUsage.getId()));

        if (chatbotDailyUsage.getId() != null) {
            entity.updateUsage(chatbotDailyUsage.getCallCount(), chatbotDailyUsage.getTokenUsed());
        }

        ChatbotDailyUsageJpaEntity saved = chatbotDailyUsageJpaRepository.save(entity);
        return toDomain(saved);
    }

    // JPA 엔티티 → 도메인 모델 변환 (변환 책임은 Adapter가 갖는다는 프로젝트 컨벤션)
    private ChatbotDailyUsage toDomain(ChatbotDailyUsageJpaEntity entity) {
        return new ChatbotDailyUsage(
                entity.getId(),
                entity.getUserId(),
                entity.getUsageDate(),
                entity.getCallCount(),
                entity.getTokenUsed(),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }

}
