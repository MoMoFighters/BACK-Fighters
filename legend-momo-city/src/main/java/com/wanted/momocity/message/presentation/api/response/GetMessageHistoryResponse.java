package com.wanted.momocity.message.presentation.api.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.wanted.momocity.message.application.usecase.MessageQueryUseCase.MessageHistoryView;

import java.time.LocalDateTime;
import java.util.List;

public record GetMessageHistoryResponse(
        RoomInfo roomInfo
) {
    public record RoomInfo(
            Long roomId,
            Long inMemberCount, //로그인 유저 포함 멤버수
            String roomTitle,
            List<MemberInfo> memberInfo,
            List<Message> messages
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

    public record Message(
            Long messageId, //일반 메시지 또는 안내 문구
            Long senderId,
            String name,
            String nickname,
            String role,
            String status,
            String content,
            LocalDateTime createdAt, //메시지 시간 또는 안내 문구
            Long unreadCount, //말풍선을 읽지 않은 사람 수
            boolean isMine,
            boolean isLeftRoom,
            String profileImageUrl,
            Long targetUserId, //안내 문구에 해당하는 사람(ex. 초대된 사람, 나간 사람, 이름 바꾼 사람)
            String type //안내 문구 타입
    ) {}

    public static GetMessageHistoryResponse of(MessageHistoryView view) {

        //채팅방 멤버 정보 가공
        List<MemberInfo> memberInfos = view.memberInfo().stream()
                .map(member -> {
                    String targetName = null;
                    String targetNickname = member.nickname();
                    String targetLectureTitle = null;

                    if ("TEACHER".equals(member.role())) {
                        targetName = member.name();
                        if(member.lectureTitle() != null && !member.lectureTitle().isEmpty()) {
                            targetLectureTitle = "(" + String.join(",", member.lectureTitle()) + ")";
                        }
                    } else if ("STUDENT".equals(member.role())) {
                        targetName = null;
                    }

                    return new MemberInfo(
                            member.userId(),
                            targetName,
                            targetNickname,
                            targetLectureTitle,
                            member.role(),
                            member.status(),
                            member.profileImageUrl(),
                            member.isLeftRoom()
                    );
                }).toList();

        List<Message> detailList = view.messages().stream()
                .map(msg -> {
                    String displayNickname = msg.nickname();
                    String finalLectureTitle = null;

                    //상대방이 보낸 말풍선 가공
                    if (msg.isMine()) {
                        displayNickname = msg.nickname();
                    } else if (!"me".equals(msg.status())) {
                        //내가 쓴 글이나 나와의 채팅이 아닌, 상대 메시지 가공
                        if (!view.isNotActive() && (view.shouldMasked() || displayNickname == null || displayNickname.isEmpty() || msg.isLeftRoom())) {
                            if (displayNickname == null || displayNickname.isEmpty()) {
                                //역추적 후에도 나간 상대방 식별 불가
                                displayNickname = "(알 수 없음)";
                            } else {
                                //상대방 식별 가능하지만 친구 아니거나 나간 경우
                                displayNickname += "(알 수 없음)";
                            }
                        }
                     }

                    return new Message(
                            msg.messageId(),
                            msg.senderId(),
                            "TEACHER".equals(msg.role()) && !msg.isMine() ? msg.name() : null,
                            displayNickname,
                            msg.role(),
                            msg.status(),
                            msg.content(),
                            msg.createdAt(),
                            msg.unreadCount(),
                            msg.isMine(),
                            msg.isLeftRoom(),
                            msg.profileImageUrl(),
                            msg.targetUserId(),
                            msg.type()
                    );
                }).toList();

        RoomInfo roomInfo = new RoomInfo(
                view.roomInfo().roomId(),
                view.roomInfo().inMemberCount(),
                view.roomInfo().roomTitle(),
                memberInfos,
                detailList
        );

        return new GetMessageHistoryResponse(roomInfo);
    }
}
