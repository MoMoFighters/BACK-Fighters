package com.wanted.momocity.chatbot.presentation.api.common;

import com.wanted.momocity.chatbot.domain.exception.ChatbotDailyLimitExceededException;
import com.wanted.momocity.chatbot.domain.exception.ChatbotInvalidQuestionException;
import com.wanted.momocity.chatbot.domain.exception.ChatbotLectureNotFoundException;
import com.wanted.momocity.chatbot.domain.exception.PolicySearchException;
import com.wanted.momocity.global.presentation.api.common.ApiErrorResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/* comment.
    지금까지 비어있던 챗봇 전용 예외 처리기이다.
    도메인 예외 4개(신규 질문 검증 + 기존 3개) 를 각각 알맞은 HTTP 상태로 변환해서 응답
 */

@RestControllerAdvice(basePackages = "com.wanted.momocity.chatbot")
public class ChatbotExceptionHandler {

    // 질문이 비어있거나 255자를 초과했을 때 (400)
    @ExceptionHandler(ChatbotInvalidQuestionException.class)
    public ResponseEntity<ApiErrorResponse> handleInvalidQuestion(ChatbotInvalidQuestionException exception) {
        return ResponseEntity.badRequest()
                .body(ApiErrorResponse.of(
                        HttpStatus.BAD_REQUEST.value(),
                        ChatbotResponseCode.INVALID_QUESTION,
                        exception.getMessage()
                ));
    }

    // 하루 호출 한도를 넘겼을 때 (400)
    @ExceptionHandler(ChatbotDailyLimitExceededException.class)
    public ResponseEntity<ApiErrorResponse> handleDailyLimitExceeded(ChatbotDailyLimitExceededException exception) {
        return ResponseEntity.badRequest()
                .body(ApiErrorResponse.of(
                        HttpStatus.BAD_REQUEST.value(),
                        ChatbotResponseCode.DAILY_LIMIT_EXCEEDED,
                        exception.getMessage()
                ));
    }

    // 존재하지 않는 강의로 질문했을 때 (404)
    @ExceptionHandler(ChatbotLectureNotFoundException.class)
    public ResponseEntity<ApiErrorResponse> handleLectureNotFound(ChatbotLectureNotFoundException exception) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ApiErrorResponse.of(
                        HttpStatus.NOT_FOUND.value(),
                        ChatbotResponseCode.LECTURE_NOT_FOUND,
                        exception.getMessage()
                ));
    }

    // momo-ai(외부 파이썬 서버) 호출 실패했을 때 (502 — 우리 서버가 아닌 외부 의존 서비스 문제)
    @ExceptionHandler(PolicySearchException.class)
    public ResponseEntity<ApiErrorResponse> handlePolicySearchFailed(PolicySearchException exception) {
        return ResponseEntity.status(HttpStatus.BAD_GATEWAY)
                .body(ApiErrorResponse.of(
                        HttpStatus.BAD_GATEWAY.value(),
                        ChatbotResponseCode.POLICY_SEARCH_FAILED,
                        exception.getMessage()
                ));
    }

}
