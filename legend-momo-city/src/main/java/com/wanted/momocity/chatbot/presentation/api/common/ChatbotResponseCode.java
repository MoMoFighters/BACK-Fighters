package com.wanted.momocity.chatbot.presentation.api.common;

/* comment.
    챗봇 도메인 전용 API 응답 코드 상수 모음
    CHATBOT 접두사
 */

public class ChatbotResponseCode {

    private ChatbotResponseCode() {}

    // ===== 성공 2xx (사용량 조회 GET 성공 시 응답 코드)=====
    public static final String USAGE_FETCHED =
            "CHATBOT-USAGE-FETCHED";

    // ===== 실패 4xx, 5xx(도메인 예외 3개와 1:1 매칭) =====
    public static final String DAILY_LIMIT_EXCEEDED =
            "CHATBOT-DAILY-LIMIT-EXCEEDED";

    // ChatbotLectureNotFoundException 매핑용
    public static final String LECTURE_NOT_FOUND =
            "CHATBOT-LECTURE-NOT-FOUND";

    // PolicySearchException 모모 서버 연결 실패할 경우
    public static final String
    POLICY_SEARCH_FAILED = "CHATBOT-POLICY-SEARCH-FAILED";

}
