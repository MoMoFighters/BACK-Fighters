package com.wanted.momocity.chatbot.application.usecase;

/* comment.
    챗봇 사용량(오늘 몇 번 썼는지) 조회와, 호출 전 한도 체크 + 증가를 표현 계층에 노출하는 유스케이스 인터페이스
 */

public interface ChatbotUsageUseCase {

    // 오늘 사용량 조회 (FE 원형 퍼센트 UI 용 raw 값)
    UsageStatus getTodayUsage(Long userId);

    // 호출 한도 체크 후 카운트 증가, 초과 시 ChatbotDailyLimitExceededException
    void checkAndIncrease(Long userId);

    record UsageStatus(int callCount, int dailyLimit) {}
}