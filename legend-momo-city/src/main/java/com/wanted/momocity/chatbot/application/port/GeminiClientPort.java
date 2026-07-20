package com.wanted.momocity.chatbot.application.port;

/* comment.
    제미나이 스트리밍 API 를 호출하는 방법을 정의만 하는 포트 인터페이스
    -> 실제로 WebClient 구현은 GeminiClientAdapter 담당
 */

public interface GeminiClientPort {

    void streamAnswer(String prompt, StreamCallback callback);

    // 청크마다/완료/에러마다 호출되는 콜백
    interface StreamCallback {
        void onChunk(String textChunk);
        void onComplete();
        void onError(Throwable error);
    }

}
