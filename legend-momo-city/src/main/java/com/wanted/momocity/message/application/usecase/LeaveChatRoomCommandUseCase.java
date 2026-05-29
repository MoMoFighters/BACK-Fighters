package com.wanted.momocity.message.application.usecase;

import com.wanted.momocity.message.application.command.LeaveChatRoomCommand;

public interface LeaveChatRoomCommandUseCase {
    LeaveChatRoomView handle(Long roomId, Long userId);

    record LeaveChatRoomView(
            boolean isLastMember, //마지막 남은 사람이었는지 여부
            Long roomId,
            Long userId, //남겨진 사람 아이디
            String nickname, //남겨진 사람 닉네임
            String role,
            String status
    ) {}
}
