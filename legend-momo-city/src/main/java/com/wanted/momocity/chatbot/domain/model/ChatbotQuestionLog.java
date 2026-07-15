package com.wanted.momocity.chatbot.domain.model;

/* comment.
    유저가 챗봇에게 던진 질문 하나를 기록하는 도메인 모델 (유사 질문 3회 판별)
 */

import java.time.LocalDateTime;

public class ChatbotQuestionLog {

    private Long id;
    private final Long userId;
    private final Long lectureId;
    private final String question;
    private final boolean faqMatched;
    private LocalDateTime createdAt;

    // 질문이 들어올 때마다 새롭게 맏느는 생성용 생성자이다.
    // (id, createdAt 은 DB 에서 채워진다)
    public ChatbotQuestionLog(Long userId, Long lectureId, String question, boolean faqMatched) {
        this.userId = userId;
        this.lectureId = lectureId;
        this.question = question;
        this.faqMatched = faqMatched;
    }

    // DB 에서 조회해온 값을 그대로 복원할 때 쓰는 생성자
    public ChatbotQuestionLog(Long id, Long userId, Long lectureId, String question,
                              boolean faqMatched, LocalDateTime createdAt) {
        this.id = id;
        this.userId = userId;
        this.lectureId = lectureId;
        this.question = question;
        this.faqMatched = faqMatched;
        this.createdAt = createdAt;
    }

    // 강의 페이지에서 열려서 던진 질문인지? 아니면 다른 페이지에서 열린 페이지인지 판단
    public boolean isAboutLecture() {
        return lectureId != null;
    }

    // Getter
    public Long getId() { return id; }
    public Long getUserId() { return userId; }
    public Long getLectureId() { return lectureId; }
    public String getQuestion() { return question; }
    public boolean isFaqMatched() { return faqMatched; }
    public LocalDateTime getCreatedAt() { return createdAt; }



}
