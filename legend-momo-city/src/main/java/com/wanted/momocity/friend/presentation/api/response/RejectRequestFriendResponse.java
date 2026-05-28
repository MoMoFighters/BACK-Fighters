package com.wanted.momocity.friend.presentation.api.response;

import com.wanted.momocity.friend.application.usecase.RejectRequestFriendCommandUseCase.RejectView;

public record RejectRequestFriendResponse(
        Long userId,
        String nickname,
        String role,
        String status
) {
    public static RejectRequestFriendResponse from(RejectView view) {
        return new RejectRequestFriendResponse(
                view.userId(),
                view.nickname(),
                view.role(),
                view.status()
        );
    }
}
