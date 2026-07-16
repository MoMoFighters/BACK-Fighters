package com.wanted.momocity.chatbot.application.service;

import com.wanted.momocity.chatbot.application.port.GeminiClientPort;
import com.wanted.momocity.chatbot.application.port.LectureInfoPort;
import com.wanted.momocity.chatbot.application.port.PolicySearchPort;
import com.wanted.momocity.chatbot.application.port.ReviewInfoPort;
import com.wanted.momocity.chatbot.application.support.ChatbotPromptBuilder;
import com.wanted.momocity.chatbot.application.usecase.ChatbotQuestionUseCase;
import com.wanted.momocity.chatbot.application.usecase.ChatbotUsageUseCase;
import com.wanted.momocity.chatbot.domain.exception.ChatbotLectureNotFoundException;
import com.wanted.momocity.chatbot.domain.model.ChatbotQuestionLog;
import com.wanted.momocity.chatbot.domain.repository.ChatbotQuestionLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * 질문 하나가 들어왔을 때 전체 흐름을 조율하는 서비스이다.
 * 1. 유사 질문 3번째인지 판별한다. - 만약 맞다면 제미나이를 호출하지 않고 안내 메세지로 끝
 * 2. 강의 질문/정책 질문 분기 나누기 - 컨텍스트 수집
 * 3. 사용량 체크 + 증가
 * 4. 프롬포트 조립 + 질문 로그 저장
 * 5. GeminiClientPort 로 스트리밍 호출, 그 콜백을 Controller 쪽 콜백에 그대로 연결
 */

@Service
@RequiredArgsConstructor
public class ChatbotQuestionService implements ChatbotQuestionUseCase {

    // 중복되는 값 허락은 2회까지만
    private static final int SIMILAR_QUESTION_THRESHOLD = 2;
    private static final String DUPLICATE_QUESTION_GUIDE_MESSAGE =
            "이미 비슷한 질문을 주셨네요. 궁금하신 내용은 yourmomocity@gmail.com 으로 문의 부탁드립니다!";

    // 모든 조사 패턴 케이스 분석
    private static final Pattern JOSA = Pattern.compile(
            "(으로|에서|에게|한테|까지|부터|이나|이란|라는|은|는|이|가|을|를|의|도|만|로|와|과|나)$"
    );

    // 모든 형용사 조사 패턴 케이스 분석
    private static final Set<String> STOPWORDS = Set.of(
            "어떻게", "왜", "언제", "어디", "어디서", "무엇", "뭐", "좀", "저", "제가", "혹시", "그냥"
    );

    private final ChatbotUsageUseCase chatbotUsageUseCase;
    private final ChatbotQuestionLogRepository chatbotQuestionLogRepository;
    private final ChatbotPromptBuilder chatbotPromptBuilder;
    private final GeminiClientPort geminiClientPort;
    private final LectureInfoPort lectureInfoPort;
    private final ReviewInfoPort reviewInfoPort;
    private final PolicySearchPort policySearchPort;

    // 전체 흐름을 통솔한다. 판별 -> 컨텍스트 -> 프롬프트 -> 로그 -> Gemini 순서
    // 실패하면 callback.onError 로 통일해서 던짐
    @Override
    public void ask(AskCommand command, AnswerCallback callback) {
        try {
            if (isDuplicateQuestion(command)) {
                chatbotQuestionLogRepository.save(new ChatbotQuestionLog(
                        command.userId(), command.lectureId(), command.question(), false));
                callback.onChunk(DUPLICATE_QUESTION_GUIDE_MESSAGE);
                callback.onComplete();
                return;
            }

            ChatbotPromptBuilder.PromptContext context = buildContext(command);

            chatbotUsageUseCase.checkAndIncrease(command.userId());

            String prompt = chatbotPromptBuilder.build(context);
            chatbotQuestionLogRepository.save(new ChatbotQuestionLog(
                    command.userId(), command.lectureId(), command.question(),
                    !context.policyResults().isEmpty()));

            geminiClientPort.streamAnswer(prompt, toGeminiCallback(callback));
        } catch (Exception e) {
            callback.onError(e);
        }
    }

    // 오늘 자정 이후 질문들 중, 핵심 단어가 겹치는 게 2개 이상이면 이번 질문이 3번째 반복
    private boolean isDuplicateQuestion(AskCommand command) {
        List<ChatbotQuestionLog> recentLogs = chatbotQuestionLogRepository
                .findRecentByUserId(command.userId(), LocalDate.now().atStartOfDay());

        long similarCount = recentLogs.stream()
                .filter(log -> isSimilar(log.getQuestion(), command.question()))
                .count();

        return similarCount >= SIMILAR_QUESTION_THRESHOLD;
    }

    // lectureId 있으면 강의 질문(강의정보+수강평), 없으면 정책/FAQ 질문(RAG 검색)
    // 강의인데 없는 lecturedId 면 ChatbotLectureNotFoundException
    private ChatbotPromptBuilder.PromptContext buildContext(AskCommand command) {
        if (command.lectureId() != null) {
            LectureInfoPort.LectureSummary summary = lectureInfoPort
                    .findLectureSummary(command.lectureId())
                    .orElseThrow(ChatbotLectureNotFoundException::new);
            List<String> reviews = reviewInfoPort.getReviewContents(command.lectureId());
            return new ChatbotPromptBuilder.PromptContext(command.question(), summary, reviews, List.of());
        }

        List<String> policyResults = policySearchPort.search(command.question());
        return new ChatbotPromptBuilder.PromptContext(command.question(), null, List.of(), policyResults);
    }

    // 조사 제거 + 불용어 제외 후, 핵심 단어가 하나라도 겹치면 유사로 판단
    private boolean isSimilar(String a, String b) {
        return !Collections.disjoint(normalize(a), normalize(b));
    }

    private Set<String> normalize(String text) {
        Set<String> result = new HashSet<>();
        for (String token : text.split("\\s+")) {
            String stripped = JOSA.matcher(token).replaceAll("");
            if (stripped.length() >= 2 && !STOPWORDS.contains(stripped)) {
                result.add(stripped);
            }
        }
        return result;
    }

    // GeminiClientPort 의 콜백을 그대로 UseCase 콜백에 연결만 함 (변환 책임만 짐)
    // ChatbotQuestionUseCase 의 콜백 모양으로 그대로 갈아 끼우는 어댑터 역할 private 메서드
    private GeminiClientPort.StreamCallback toGeminiCallback(AnswerCallback callback) {
        return new GeminiClientPort.StreamCallback() {
            // Gemini 가 청크 하나 줄 때마다 실행 받은 텍스트를 Controller 콜백으로 그대로 전달
            @Override
            public void onChunk(String textChunk) {
                callback.onChunk(textChunk);
            }

            // Gemini 스트리밍 끝났을 때 실행, Controller 콜백에 "끝냈다" 신호 전달
            @Override
            public void onComplete() {
                callback.onComplete();
            }

            // Gemini 쪽에서 에러 나면 실행, Controller 콜백에 에러 그대로 전달
            @Override
            public void onError(Throwable error) {
                callback.onError(error);
            }
        };
    }
}
