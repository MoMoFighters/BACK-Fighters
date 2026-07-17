package com.wanted.momocity.chatbot.presentation.api.request;

/* comment.
    챗봇 질문 요청 DTO 이다.
    lectureId 는 nullable 처리
    강의 페이지 밖에서 질문하면 null 로 와서 정책/FAQ 질문으로 처리되게 로직 구현
 */

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ChatbotQuestionRequest(
        Long lectureId,

        // 질문이 공백인걸 막아주는 역할
        @NotBlank(message = "질문 내용을 입력해주세요!!")
        // 최대 몇 자까지 입력가능한지 처리
        @Size(max = 255, message = "질문은 255자 이하로 입력해주세요!")
        String question
) {
}
