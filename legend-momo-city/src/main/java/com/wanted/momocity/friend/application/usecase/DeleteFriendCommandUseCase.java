package com.wanted.momocity.friend.application.usecase;

import com.wanted.momocity.friend.application.command.DeleteFriendCommand;

public interface DeleteFriendCommandUseCase {
    DeleteView handle(DeleteFriendCommand command);

    record DeleteView(
            Long userId,
            String nickname,
            String role,
            String status
    ) {}
}
