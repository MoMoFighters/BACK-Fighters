package com.wanted.momocity.chatbot.application.usecase;

/* comment.
    질문을 받아서 답변을 콜백으로 흘려주는 유스케이스이다.
    SSE 를 직접 몰라도 되게, 콜백 인터페이스 결과를 전달한다.
    GeminiClientPort 와 동일한 콜백 패턴이다)
 */

public interface ChatbotQuestionUseCase {

    // Controller 가 호출하는 진입점, 질문 정보와 콜백을 받음
    void ask(AskCommand command, AnswerCallback callback);

    // 유저 ID, 강의 ID 질문 텍스트
    record AskCommand(Long userId, Long lectureId, String question) {}

    // 청크마다/완료/에러마다 호출되는 콜백, SseEmitter 는 Controller 가 콜백 구현체를 안에서 다시 만든다.
    interface AnswerCallback {
        void onChunk(String textChunk);
        void onComplete();
        void onError(Throwable error);
    }

}
