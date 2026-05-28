package com.wanted.momocity.friend.application.usecase;

import com.wanted.momocity.friend.application.command.UnblockFriendCommand;

public interface UnblockFriendCommandUseCase {
    UnblockView handle(UnblockFriendCommand command);

    record UnblockView(
            Long userId,
            String nickname,
            String role,
            String status
    ) {}
}
