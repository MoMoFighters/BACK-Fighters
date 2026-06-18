package com.wanted.momocity.message.presentation.api.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.wanted.momocity.message.application.usecase.MessageQueryUseCase.ChatRoomView;

import java.time.LocalDateTime;
import java.util.List;

//채팅방 목록
public record FindChatRoomResponse(
        RoomInfo roomInfo

) {
    public record RoomInfo(
            Long roomId,
            String roomTitle,
            Long inMemberCount,
            String content, //마지막 메시지
            LocalDateTime createdAt, //메시지 시간, 안내 문구 시간, 둘다 없으면 개설 시간
            Long unreadCount,
            List<MemberInfo> memberInfo
    ) {}

    public record MemberInfo(
        Long userId,
        String name,
        String nickname,
        String lectureTitle,
        String role,
        String status,
        String profileImageUrl,
        boolean isLeftRoom
    ) {}


    public static FindChatRoomResponse from(ChatRoomView view) {
        //🚨다대다에서 roomInfo, memberInfo로 줄 땐 닉네임 가공X(메시지 내역에서 가공)
        //v2 -> 일대일의 경우도 있으므로 가공해야함
        //ACTIVE아니면 (알 수 없음) 가공
        String displayNickname = (view.nickname() != null) ? view.nickname() : "";
        String finalLectureTitle = null;

        //나와의 채팅 가공
        if ("me".equals(view.status())) {
            displayNickname = "나와의 채팅" + "(" + displayNickname + ")";
            //ACTIVE이면서 친구가 아닌 경우
        }
         else if (!view.isNotActive() && (view.shouldMasked() || displayNickname.isEmpty() || view.isLeftRoom())) {
            //나간 채팅방 처리
            //원래 닉네임이 있었다면 "홍길동(알 수 없음)", null이었다면 그냥 "(알 수 없음)"이 됩니다!
            if (displayNickname.isEmpty()) {
                displayNickname = "(알 수 없음)";
            } else {
                //ACTIVE가 아니거나 차단 혹은 친구 삭제 상태일 때
                //->v2 변경: ACTIVE이면서 친구가 아닐 때
                displayNickname += "(알 수 없음)";
            }
        }

        List<String> lectureTitle = view.lectureTitle();
        if (lectureTitle != null && !lectureTitle.isEmpty()) {
            finalLectureTitle = "(" + String.join(", ", lectureTitle) + ")";
        }

        MemberInfo member = new MemberInfo(
                view.userId(),
                "TEACHER".equals(view.role()) ? view.name() : null,
                displayNickname,
                finalLectureTitle,
                view.role(),
                view.status(),
                view.profileImageUrl(),
                view.isLeftRoom()
        );

        RoomInfo room = new RoomInfo(
                view.roomId(),
                view.roomTitle(),
                view.inMemberCount(),
                view.content(),
                view.createdAt(),
                view.unreadCount(),
                List.of(member)
        );

        return new FindChatRoomResponse(room);
    }
}
