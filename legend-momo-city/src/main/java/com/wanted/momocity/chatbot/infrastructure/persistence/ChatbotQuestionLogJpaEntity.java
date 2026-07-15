package com.wanted.momocity.chatbot.infrastructure.persistence;

/* comment.
    chatbot_question_log 테이블 멥핑
    유저가 던진 질문 1건을 기록, 유사 질문 3회 판별용 데이터
 */

import jakarta.persistence.*;
import lombok.Getter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Getter
@Entity
@Table(name = "chatbot_question_log")
// created_at 자동 세팅
@EntityListeners(AuditingEntityListener.class)
public class ChatbotQuestionLogJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId; // 누가 물어봤는지

    @Column(name = "lecture_id")
    private Long lectureId; // 강의 페이지에서 던진 질문이면 값 있음, 아니면 null

    @Column(name = "question", length = 100)
    private String question; // 실제 질문 텍스트 (DDL: VARCHAR(100))

    @Column(name = "is_faq_matched")
    private boolean faqMatched; // FAQ랑 매칭됐는지 여부 (생성 시점에 항상 결정됨)

    @CreatedDate
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt; // insert 시점에 자동 세팅, 이후 변경 불가

    protected ChatbotQuestionLogJpaEntity() {
    }

    public ChatbotQuestionLogJpaEntity(Long userId, Long lectureId, String question, boolean faqMatched) {
        this.userId = userId;
        this.lectureId = lectureId;
        this.question = question;
        this.faqMatched = faqMatched;
    }

}
