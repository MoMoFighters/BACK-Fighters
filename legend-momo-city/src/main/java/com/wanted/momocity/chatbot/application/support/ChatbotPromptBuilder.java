package com.wanted.momocity.chatbot.application.support;

/* comment.
    강의 정보, 수강생, 정책 검색 결과와 질문을 조합해서
    Gemini 에게 보낼 최종 프롬포트 문자열을 만드는 클래스이다.
    포트를 직접적으로 호출하지 않고, 이미 조회된 데이터만 받아서 문자열만 조립한다.
    어떤 포트를 부를지 판단하는 것은 ChatbotQuestionService 의 책임)
 */

import com.wanted.momocity.chatbot.application.port.LectureInfoPort;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ChatbotPromptBuilder {

    // 시스템 지문 (강의 정보, 수강평 블럭 또는 정체 검색 블럭) -> 질문
    // 순서대로 이어붙여서 최종 문자열 반환
    public String build(PromptContext context) {
        StringBuilder prompt = new StringBuilder();
        prompt.append("당신은 온라인 강의 플랫폼 모모시티의 고객 지원 챗봇입니다. ")
                .append("당신의 이름은 '모아이' 입니다. 자신을 소개하거나 인사할 때는 반드시 '모아이' 라는 이름을 사용하세요. ")
                .append("아래 제공된 정보만 근거로 친절하고 정확하게 답변하세요. ")
                .append("제공된 정보에 없는 내용은 절대 추측하거나 지어내지 마세요. ")
                .append("만약 질문에 답할 근거가 아래 정보에 없다면, ")
                .append("\"해당 내용은 확인되지 않습니다\"라고 명확히 답변하세요. ")
                .append("답변은 마크다운 형식(굵게, 목록 등)을 활용해 가독성 있게 작성하세요.\n\n");


        //강의 관련 질문이면 강의 정보 + 수강평 블럭 추가
        if (context.lectureSummary() != null) {
            prompt.append("[강의 정보]\n제목: ").append(context.lectureSummary().title()).append("\n")
                    .append("설명: ").append(context.lectureSummary().description()).append("\n\n");

            if (!context.reviews().isEmpty()) {
                prompt.append("[수강평]\n");
                context.reviews().forEach(review -> prompt.append("- ").append(review).append("\n"));
                prompt.append("\n");
            }
            // 분기 처리 - 강의와 무관한 질문일 경우 RAG 검색 결과 블럭 추가하기
        } else if (!context.recommendations().isEmpty()) {
            prompt.append("[추천 강의 후보]\n");
            context.recommendations().forEach(rec -> prompt.append("- ").append(rec.title())
                    .append(" (평점 ").append(rec.averageRating()).append(")\n  ")
                    .append(rec.description()).append("\n"));
            prompt.append("\n위 후보 중에서만 골라 추천 이유와 함께 안내하세요.\n\n");
        } else if (!context.policyResults().isEmpty()) {
            prompt.append("[관련 정책/FAQ]\n");
            context.policyResults().forEach(policy -> prompt.append("- ").append(policy).append("\n"));
            prompt.append("\n");

        } else {
            prompt.append("[관련 정보]\n검색된 관련 정보가 없습니다.\n\n");
        }

        prompt.append("[질문]\n").append(context.question());
        return prompt.toString();
    }

    // 이미 조회가 끝난 데이터만 담는 순수 데이터 컨테이너
    // LectureSummary 타입은 새로 안만들고 LectureInfoPort 에 이미 있는 내용 재사용
    public record PromptContext(
            String question,
            LectureInfoPort.LectureSummary lectureSummary,
            List<String> reviews,
            List<String> policyResults,
            List<LectureInfoPort.LectureRecommendation> recommendations
    ) {}

}
