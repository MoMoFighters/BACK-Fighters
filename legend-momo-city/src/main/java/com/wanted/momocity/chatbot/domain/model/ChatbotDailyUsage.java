package com.wanted.momocity.chatbot.domain.model;

/* comment.
    유저 한 명의 하루치 챗봇 호출 횟수를 표현하고, 하루 40회 한도를 스스로 판단
    그리고 증가시기키는 도메인 모델이다.
 */

import com.wanted.momocity.chatbot.domain.exception.ChatbotDailyLimitExceededException;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class ChatbotDailyUsage {

    private static final int DAILY_LIMIT = 40;

    private Long id;
    private final Long userId;
    private final LocalDate usageDate;
    private int callCount;
    private Integer tokenUsed;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;


    // 오늘 처음 호출하는 유저를 위한 신규 생성용 생성자 (id 는 DB 에서 채움)
    public ChatbotDailyUsage(Long userId, LocalDate usageDate) {
        this.userId = userId;
        this.usageDate = usageDate;
        this.callCount = 0;
    }

    // DB 에서 조회해온 값을 그대로 복원할 때 쓰는 생성자
    public ChatbotDailyUsage(Long id, Long userId, LocalDate usageDate, int callCount,
                             Integer tokenUsed, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.userId = userId;
        this.usageDate = usageDate;
        this.callCount = callCount;
        this.tokenUsed = tokenUsed;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    // 하루 호출 한도(40회)를 넘겼는지 판단
    public boolean isLimitExceeded() {
        return callCount >= DAILY_LIMIT;
    }

    // 호출 1회 사용 처리, 한도 넘으면 예외
    public void increaseCallCount() {
        if (isLimitExceeded()) {
            throw new ChatbotDailyLimitExceededException();
        }
        this.callCount++;
    }

    // getter 구문
    public Long getId() { return id; }
    public Long getUserId() { return userId; }
    public LocalDate getUsageDate() { return usageDate; }
    public int getCallCount() { return callCount; }
    // DAILY_LIMIT 가 private static final 이라 밖에 못 꺼낸다.
    public int getDailyLimit() { return DAILY_LIMIT; }
    public Integer getTokenUsed() { return tokenUsed; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }

}
