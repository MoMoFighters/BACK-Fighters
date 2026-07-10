package com.wanted.momocity.chatbot.domain.exception;

// 하루 호출 한도(5회) 초과 시 사용하는 챗봇 전용 예외. ChatbotExceptionHandler가 직접 처리한다.
public class ChatbotDailyLimitExceededException extends RuntimeException {
}
