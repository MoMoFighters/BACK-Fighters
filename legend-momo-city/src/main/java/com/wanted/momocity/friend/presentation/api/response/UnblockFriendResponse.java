package com.wanted.momocity.friend.presentation.api.response;

import com.wanted.momocity.friend.application.usecase.UnblockFriendCommandUseCase.UnblockView;

public record UnblockFriendResponse(
        Long userId,
        String nickname,
        String role,
        String status
) {
    public static UnblockFriendResponse from(UnblockView view) {
        return new UnblockFriendResponse(
                view.userId(),
                view.nickname(),
                view.role(),
                view.status()
        );
    }
}
