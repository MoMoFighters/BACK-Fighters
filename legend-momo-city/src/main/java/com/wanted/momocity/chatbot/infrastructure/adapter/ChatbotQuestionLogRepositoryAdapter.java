package com.wanted.momocity.chatbot.infrastructure.adapter;

/* comment.
    도메인 포트 ChatbotQuestionLogRepository 구현
    질문 로그는 항상 새로 insert 만 하기 때문에 DailyUsage 보다 훨씬 단순하다.
 */

import com.wanted.momocity.chatbot.domain.model.ChatbotQuestionLog;
import com.wanted.momocity.chatbot.domain.repository.ChatbotQuestionLogRepository;
import com.wanted.momocity.chatbot.infrastructure.persistence.ChatbotQuestionLogJpaEntity;
import com.wanted.momocity.chatbot.infrastructure.persistence.ChatbotQuestionLogJpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public class ChatbotQuestionLogRepositoryAdapter implements ChatbotQuestionLogRepository {

    private final ChatbotQuestionLogJpaRepository chatbotQuestionLogJpaRepository;

    public ChatbotQuestionLogRepositoryAdapter(ChatbotQuestionLogJpaRepository chatbotQuestionLogJpaRepository) {
        this.chatbotQuestionLogJpaRepository = chatbotQuestionLogJpaRepository;
    }

    // DailyUsage 처럼 id 분기가 없음. 질문 로그는 한 번만 기록되면 끝이다.
    @Override
    // 질문 1건 = 항상 새 행 insert
    public ChatbotQuestionLog save(ChatbotQuestionLog chatbotQuestionLog) {
        ChatbotQuestionLogJpaEntity entity = new ChatbotQuestionLogJpaEntity(
                chatbotQuestionLog.getUserId(),
                chatbotQuestionLog.getLectureId(),
                chatbotQuestionLog.getQuestion(),
                chatbotQuestionLog.isFaqMatched()
        );
        return toDomain(chatbotQuestionLogJpaRepository.save(entity));
    }

    // JPA Repository가 준 List<Entity> 를 스트림으로 돌면서 각각 toDomain() 적용하고, toList() 로 다시 리스트로 모은다.
    @Override
    // since 시점 이후 질문 목록을 도메인 모델 리스트로 변환해서 반환
    public List<ChatbotQuestionLog> findRecentByUserId(Long userId, LocalDateTime since) {
        return chatbotQuestionLogJpaRepository.findByUserIdAndCreatedAtAfterOrderByCreatedAtDesc(userId, since)
                .stream()
                .map(this::toDomain)
                .toList();
    }

    // JPA 엔티티 → 도메인 모델 변환
    private ChatbotQuestionLog toDomain(ChatbotQuestionLogJpaEntity entity) {
        return new ChatbotQuestionLog(
                entity.getId(),
                entity.getUserId(),
                entity.getLectureId(),
                entity.getQuestion(),
                entity.isFaqMatched(),
                entity.getCreatedAt()
        );
    }

}
