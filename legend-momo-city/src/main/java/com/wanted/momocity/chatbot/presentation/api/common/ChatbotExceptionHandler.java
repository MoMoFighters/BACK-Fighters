package com.wanted.momocity.chatbot.presentation.api.common;

import org.springframework.web.bind.annotation.RestControllerAdvice;

// chatbot 패키지 전용 예외 처리기. 전역 ApiExceptionHandler와 별개로,
// AI 호출 관련 이슈(일일 한도 초과, 강의 없음)를 챗봇 도메인 차원에서 빠르게 구분해 처리하기 위해 분리한다.
@RestControllerAdvice(basePackages = "com.wanted.momocity.chatbot")
public class ChatbotExceptionHandler {
}
