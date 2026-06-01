package com.wanted.momocity.message.application.usecase;

public interface CreateChatRoomCommandUseCase {
    CreateRoomView handle(Long userId, Long targetUserId);

    record CreateRoomView(
            boolean isExisting, //기존 방 존재 여부
            Long roomId,
            Long targetUserId,
            String nickname,
            String role,
            String status
    ) {}
}
