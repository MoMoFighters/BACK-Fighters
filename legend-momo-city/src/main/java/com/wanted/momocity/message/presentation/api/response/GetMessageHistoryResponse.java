package com.wanted.momocity.message.presentation.api.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.wanted.momocity.message.application.usecase.GetMessageHistoryQueryUseCase.MessageHistoryView;

import java.time.LocalDateTime;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record GetMessageHistoryResponse(
        Long messageId,
        Long senderId,
        String name,
        String nickname,
        String lectureTitle,
        String role,
        String status,
        String content,
        LocalDateTime createdAt,
        boolean isRead,
        boolean isMine,
        String profileImageUrl,
        String notMeTargetName,
        String notMeNickname,
        String notMeRole,
        String notMeLectureTitle
) {
    public static GetMessageHistoryResponse from(MessageHistoryView view) {

        // 🎯 레퍼런스(목록 DTO) 정책 미러링 가공 시작
        String displayNickname = view.nickname();
        String finalLectureTitle = null;

        // 상대방이 보낸 말풍선 가공 규칙 적용
        if ("me".equals(view.status())) {
            displayNickname = "나와의 채팅" + "(" + displayNickname + ")";
        } else if (view.isMine()) {
            // 내가 보낸 메시지인 경우 마스킹 정책에서 제외하고 내 닉네임 그대로 유지
            displayNickname = view.nickname();
        } else if (view.isNotActive()) {
                // ACTIVE가 아니거나 차단, 친구 삭제(none) 상태일 때 "(알 수 없음)" 결합
                displayNickname += "(알 수 없음)";
        }


        // 강의명 가공 소스 이식
        List<String> lectureTitle = view.lectureTitle();
        if (lectureTitle != null && !lectureTitle.isEmpty()) {
            finalLectureTitle = "(" + String.join(", ", lectureTitle) + ")";
        }

        String responseNotMeTargetName = null;
        String responseNotMeNickname = null;
        String responseNotMeRole = null;
        String responseNotMeLectureTitle= null;

        if (view.isMine()) {
            responseNotMeRole = view.notMeRole(); // 상대방의 Role (STUDENT or TEACHER)

            // 💡 [정책 분기] 상대방이 강사(TEACHER)인 경우: 이름과 닉네임 둘 다 노출
            if ("TEACHER".equals(view.notMeRole())) {
                responseNotMeTargetName = view.notMeTargetName(); // 강사 실제 성함
                responseNotMeNickname = view.notMeNickname();     // 강사 닉네임
                responseNotMeLectureTitle = finalLectureTitle;
            }
            // 💡 [정책 분기] 상대방이 학생(STUDENT)인 경우: 이름 노출 차단(null), 닉네임만 노출
            else if ("STUDENT".equals(view.notMeRole())) {
                responseNotMeTargetName = null;                   // 학생 이름은 프라이버시로 인해 절대 숨김!
                responseNotMeNickname = view.notMeNickname();     // 학생 닉네임만 허용
            }
        }

        return new GetMessageHistoryResponse(
                view.messageId(),
                view.senderId(),
                "TEACHER".equals(view.role()) && !view.isMine() ? view.name() : null, // 강사일 때만 실제 성함 노출
                displayNickname,
                finalLectureTitle,
                view.role(),
                view.status(),
                view.content(),
                view.createdAt(), // T 문자열 그대로 노출
                view.isRead(),    // true
                view.isMine(),
                view.profileImageUrl(),
                responseNotMeTargetName,
                responseNotMeNickname,
                responseNotMeRole,
                responseNotMeLectureTitle
        );
    }
}
