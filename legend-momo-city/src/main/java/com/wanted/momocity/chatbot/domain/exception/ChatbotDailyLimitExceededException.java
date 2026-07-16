package com.wanted.momocity.chatbot.domain.exception;

/* comment.
    유저가 하루 5회 호출 한도를 넘겼을 때 던지는 챗봇 전용 예외
 */

public class ChatbotDailyLimitExceededException extends RuntimeException{

    public ChatbotDailyLimitExceededException() {
        super("세션 사용량이 100% 달성했습니다. 내일 다시 시도해주세요");
    }

}
