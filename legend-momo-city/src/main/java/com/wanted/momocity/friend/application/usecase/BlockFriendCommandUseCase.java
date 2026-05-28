package com.wanted.momocity.friend.application.usecase;

import com.wanted.momocity.friend.application.command.BlockFriendCommand;

public interface BlockFriendCommandUseCase {
    BlockView handle(BlockFriendCommand command);

    record BlockView(
            Long userId,
            String nickname,
            String role,
            String status
    ) {}
}
