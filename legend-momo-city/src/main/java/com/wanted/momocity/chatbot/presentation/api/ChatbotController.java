package com.wanted.momocity.chatbot.presentation.api;

import com.wanted.momocity.auth.infrastructure.security.CustomUserDetails;
import com.wanted.momocity.chatbot.application.usecase.ChatbotQuestionUseCase;
import com.wanted.momocity.chatbot.application.usecase.ChatbotUsageUseCase;
import com.wanted.momocity.chatbot.domain.exception.ChatbotInvalidQuestionException;
import com.wanted.momocity.chatbot.presentation.api.common.ChatbotResponseCode;
import com.wanted.momocity.chatbot.presentation.api.response.ChatbotUsageResponse;
import com.wanted.momocity.global.presentation.api.common.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.Map;

@Tag(name = "Chatbot", description = "Chatbot 도메인 - AI 챗봇 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/chatbot")
public class ChatbotController {

    private final ChatbotUsageUseCase chatbotUsageUseCase;
    private final ChatbotQuestionUseCase chatbotQuestionUseCase;

    // 오늘 챗봇 사용량 조회
    // GET /api/v1/chatbot/usage
    @Operation(summary = "챗봇 오늘 사용량 조회",
            description = "오늘 하루 챗봇 호출 횟수/한도 조회. 퍼센트 계산은 FE 담당")
    @GetMapping("/usage")
    /* comment.
        로직의 흐름
        인증된 유저 ID -> ChatbotUsageUseCase.getTodayUsage() 호출 ->
        ChatbotUsageResponse.of() 로 변환 -> ApiResponse.success() 로 감싸서 반환
     */
    public ResponseEntity<ApiResponse<ChatbotUsageResponse>> getTodayUsage(
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        Long userId = userDetails.getUserId();

        ChatbotUsageUseCase.UsageStatus status = chatbotUsageUseCase.getTodayUsage(userId);

        // 성공했을 때 출력 구문
        return ResponseEntity.ok(ApiResponse.success(
                ChatbotResponseCode.USAGE_FETCHED,
                "챗봇 사용량 조회에 성공했습니다.",
                ChatbotUsageResponse.of(status)
        ));
    }

    // 챗봇 질문 스트리밍
    // GET /api/v1/chatbot/questions/stream?token=...&question=...&lectureId=...
    @Operation(summary = "챗봇 질문 (SSE 스트리밍)",
            description = "EventSource 제약상 GET+쿼리파라미터 방식. token 은 ChatbotSseTokenFilter 가 처리")
    @GetMapping(value = "/questions/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)

    // AskCommand 만들어서 ask() 호출, 콜백 3개를 각각 SSE 이벤트로 반환
    public SseEmitter streamAnswer(
            @RequestParam String question,
            @RequestParam(required = false) Long lectureId,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        // @Valid 자동 검증이 안 붙는 GET+쿼리파라미터 구조라 직접적으로 검사
        if (question == null || question.isBlank()) {
            throw new ChatbotInvalidQuestionException("질문 내용을 입력해주세요!");
        }
        // DB의 chatbot_question_log.question 컬럼이 varchar(100)이라 길이 제한도 맞춤
        if (question.length() > 100) {
            throw new ChatbotInvalidQuestionException("질문은 100자 이하로 입력해주세요!");
        }

        Long userId = userDetails.getUserId();
        // 150초 샹향 : geminiWebClient 의 ResponseTimeout(120초)보다 여유를 둬서
        // WebClient가 먼저 끊키고 SseEmitter 가 그 결과를 정상적으로 받아 전달하게 함
        SseEmitter emitter = new SseEmitter(150_000L);

        // 타임아웃 시에도 "완료"가 아니라 "타임아웃으로 끊김"을 클라이언트가 구분할 수 있게 이벤트를 먼저 보냄
        emitter.onTimeout(() -> {
            sendEvent(emitter, "timeout", "응답 시간이 초과되었습니다. 다시 시도해주세요.");
            emitter.complete();
        });
        // 클라이언트 연결 종료/스트림 완료 시 정리 로그(필요시 리소스 정리 지점)
        emitter.onCompletion(() -> {});

        ChatbotQuestionUseCase.AskCommand command =
                new ChatbotQuestionUseCase.AskCommand(userId, lectureId, question);

        chatbotQuestionUseCase.ask(command, new ChatbotQuestionUseCase.AnswerCallback() {

            // 이벤트 포멧을 한곳에서만 조립, 콜백은 아래의 3개가 전부 이 메서드를 재사용한다.
            @Override
            public void onChunk(String textChunk) {
                sendEvent(emitter, "chunk", textChunk);
            }

            // AI 가 답변을 다 보냈을 때 호출된다. 클라이언트한테 done 이벤트를 보내고
            // SSE 연결 자체를 정상적으로 종료시킨다.
            @Override
            public void onComplete() {
                sendEvent(emitter, "done", null);
                emitter.complete();
            }

            // 답변 생성 도중 예외가 터졌을 때 호출된다.
            // error 이벤트를 클라이언트에 보내고, completeWithError() 로 연결을 에러 상태로 종료.
            @Override
            public void onError(Throwable error) {
                sendEvent(emitter, "error", error.getMessage());
                emitter.completeWithError(error);
            }
        });

        return emitter;
    }

    // {"type": "chunk"/"done"/"error"} 형태로 JSON 포장해서 전송
    private void sendEvent(SseEmitter emitter, String type, String content) {
        try {
            emitter.send(SseEmitter.event().data(Map.of(
                    "type", type,
                    "content", content == null ? "" : content
            )));
        } catch (IOException e) {
            emitter.completeWithError(e);
        } catch (IllegalStateException e) {
            // emitter가 이미 종료된 상태에서 send() 호출된 경우 - 조용히 무시(재종료 시도 안 함)
        }
    }

}