package com.wanted.momocity.chatbot.infrastructure.persistence;

/* comment.
    유저가 특정 시점 이후에 남긴 질문 목록을 조회
    유사 질문 3회 판별 로직의 재료 데이터를 가져오는 저장소
 */

import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface ChatbotQuestionLogJpaRepository extends JpaRepository<ChatbotQuestionLogJpaEntity, Long> {

    // 유저가 since 시점 이후에 남긴 질문들을 최신순으로 조회 (도메인 포트 findRecentByUserId 대응)
    List<ChatbotQuestionLogJpaEntity> findByUserIdAndCreatedAtAfterOrderByCreatedAtDesc
            (Long userId, LocalDateTime since);

}
