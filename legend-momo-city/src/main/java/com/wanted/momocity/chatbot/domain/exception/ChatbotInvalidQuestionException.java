package com.wanted.momocity.chatbot.domain.exception;

/* comment.
    챗봇의 질문이 비어있거나 질문의 컨텍스트 양이 100 자 이상을 넘었을 경우 던지는
    챗봇 도메인 전용 검증 예외이다.
 */

public class ChatbotInvalidQuestionException extends RuntimeException{

    // 빈 값/글자수 초과 두 케이스마다 다른 메시지를 넣어서 던질 수 있게 메시지로 파라미터를 받음
    public ChatbotInvalidQuestionException(String message) {
        super(message);
    }

}
