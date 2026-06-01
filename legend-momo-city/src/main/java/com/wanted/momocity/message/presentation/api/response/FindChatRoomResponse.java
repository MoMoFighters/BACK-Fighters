package com.wanted.momocity.message.presentation.api.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.wanted.momocity.message.application.usecase.FindChatRoomQueryUseCase.ChatRoomView;

import java.time.LocalDateTime;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record FindChatRoomResponse(
        Long userId,
        String name,
        String nickname,
        String lectureTitle,
        String role,
        String status,
        Long roomId,
        String content,
        LocalDateTime createdAt,
        Long unreadCount,
        String profileImageUrl
) {
    public static FindChatRoomResponse from(ChatRoomView view) {
        //ACTIVE아니면 (알 수 없음) 가공
        String displayNickname = (view.nickname() != null) ? view.nickname() : "";
        String finalLectureTitle = null;

        //나와의 채팅 가공
        if ("me".equals(view.status())) {
            displayNickname = "나와의 채팅" + "(" + displayNickname + ")";
        } else if (view.isNotActive()) {
            //원래 닉네임이 있었다면 "홍길동(알 수 없음)", null이었다면 그냥 "(알 수 없음)"이 됩니다!
            if (displayNickname.isEmpty()) {
                displayNickname = "(알 수 없음)";
            } else {
                //ACTIVE가 아니거나 차단 혹은 친구 삭제 상태일 때
                displayNickname += "(알 수 없음)";
            }
        }

        List<String> lectureTitle = view.lectureTitle();
        if (lectureTitle != null && !lectureTitle.isEmpty()) {
            finalLectureTitle = "(" + String.join(", ", lectureTitle) + ")";
        }

        return new FindChatRoomResponse(
                view.userId(),
                "TEACHER".equals(view.role()) ? view.name() : null,
                displayNickname,
                finalLectureTitle,
                view.role(),
                view.status(),
                view.roomId(),
                view.content(),
                view.createdAt(),
                view.unreadCount(),
                view.profileImageUrl()
        );
    }
}
