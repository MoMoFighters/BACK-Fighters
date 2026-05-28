package com.wanted.momocity.friend.application.usecase;

import com.wanted.momocity.friend.application.command.AcceptRequestFriendCommand;

public interface AcceptRequestFriendCommandUseCase {

    AcceptView handle(AcceptRequestFriendCommand command);

    record AcceptView(
            Long userId,
            String nickname,
            String role,
            String status
    ) {}
}
