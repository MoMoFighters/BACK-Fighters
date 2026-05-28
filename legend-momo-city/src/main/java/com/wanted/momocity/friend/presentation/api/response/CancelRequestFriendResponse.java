package com.wanted.momocity.friend.presentation.api.response;

import com.wanted.momocity.friend.application.usecase.CancelRequestFriendCommandUseCase.CancelRequestFriendView;

public record CancelRequestFriendResponse(
        Long userId,
        String nickname,
        String role,
        String status
) {
    public static CancelRequestFriendResponse from(CancelRequestFriendView view) {
        return new CancelRequestFriendResponse(
                view.userId(),
                view.nickname(),
                view.role(),
                view.status()
        );
    }
}
