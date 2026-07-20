package com.wanted.momocity.chatbot.presentation.api.response;

/* comment
    사용량 조회 GET 응답 DTO 이다.
    FE 는 이 값을 통해서 원형 퍼센트 UI 를 직접적으로 계산해서 그리게 된다.
 */

import com.wanted.momocity.chatbot.application.usecase.ChatbotUsageUseCase;

// application 계층의 UsageStatus 와 필드가 똑같지만,
// 아래의 내용은 API 계약이고, UsageStatus 는 내부 값 객체라 별개로 유지했음.
public record ChatbotUsageResponse(
        int callCount,
        int dailyLimit
) {

    // Controller 가 ChatbotUsageUseCase.getTodayUsage() 결과를 그대로
    // 이 DTO 로 바꿔주는 변환 지점이다.
    public static ChatbotUsageResponse
    of(ChatbotUsageUseCase.UsageStatus status) {

        return new
                ChatbotUsageResponse(status.callCount(), status.dailyLimit());
    }

}
