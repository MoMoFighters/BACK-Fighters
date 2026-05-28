package com.wanted.momocity.friend.presentation.api.response;

import com.wanted.momocity.friend.application.usecase.BlockFriendCommandUseCase.BlockView;

public record BlockFriendResponse(
        Long userId,
        String nickname,
        String role,
        String status
) {
    public static BlockFriendResponse from(BlockView view) {
        return new BlockFriendResponse(
                view.userId(),
                view.nickname(),
                view.role(),
                view.status()
        );
    }
}
