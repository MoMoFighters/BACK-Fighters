package com.wanted.momocity.chatbot.domain.exception;

// 존재하지 않는 강의로 챗봇 질문 시 사용하는 챗봇 전용 예외. ChatbotExceptionHandler가 직접 처리한다.
public class ChatbotLectureNotFoundException extends RuntimeException {
}
