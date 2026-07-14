package com.wanted.momocity.chatbot.domain.repository;

/* comment.
    ChatbotQuestionLog 를 저장 및 조회하는 방법을 정의하는 도메인 인터페이스
    유사한 질문 3회 판별에 필요한 최근 질문 목록 조회에 포함된다.
 */

import com.wanted.momocity.chatbot.domain.model.ChatbotQuestionLog;

import java.time.LocalDateTime;
import java.util.List;

public interface ChatbotQuestionLogRepository {

    // 새 질문 로그 저장
    ChatbotQuestionLog save(ChatbotQuestionLog chatbotQuestionLog);

    // 유저가 특정 시점 이후에 남긴 질문 목록 조회 (유사 질문 비교용 재료)
    List<ChatbotQuestionLog> findRecentByUserId(Long userId, LocalDateTime since);

}