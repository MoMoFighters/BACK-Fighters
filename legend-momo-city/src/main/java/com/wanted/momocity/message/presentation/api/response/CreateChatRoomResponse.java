package com.wanted.momocity.message.presentation.api.response;

import com.wanted.momocity.message.application.usecase.MessageCommandUseCase.CreateRoomView;

import java.util.List;

public record CreateChatRoomResponse(
        RoomInfo roomInfo
) {
    public record RoomInfo(
        Long roomId,
        String roomTitle,
        Long inMemberCount,
        List<MemberInfo> memberInfo
    ) {}

    public record MemberInfo(
        Long userId,
        String name,
        String nickname,
        String role,
        String status
    ){}

    public static CreateChatRoomResponse of(CreateRoomView view) {
        List<MemberInfo> memberInfo = view.memberInfo().stream()
                .map(m -> new MemberInfo(
                        m.userId(),
                        m.name(),
                        m.nickname(),
                        m.role(),
                        m.status()
                )).toList();

        return new CreateChatRoomResponse(new RoomInfo(
                view.roomInfo().roomId(),
                view.roomInfo().roomTitle(),
                view.roomInfo().inMemberCount(),
                memberInfo
        ));
    }
}
