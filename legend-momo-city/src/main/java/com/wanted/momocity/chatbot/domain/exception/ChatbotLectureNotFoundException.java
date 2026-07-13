package com.wanted.momocity.chatbot.domain.exception;

public class ChatbotLectureNotFoundException extends RuntimeException{

    public ChatbotLectureNotFoundException() {
        super("찾으시는 강의가 존재하지 않습니다!!");
    }

}
