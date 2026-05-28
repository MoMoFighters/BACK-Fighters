package com.wanted.momocity.friend.presentation.api.response;

import com.wanted.momocity.friend.application.usecase.AcceptRequestFriendCommandUseCase.AcceptView;

public record AcceptRequestFriendResponse(
        Long userId,
        String nickname,
        String role,
        String status
) {
    public static AcceptRequestFriendResponse from(AcceptView view) {
        return new AcceptRequestFriendResponse(
                view.userId(),
                view.nickname(),
                view.role(),
                view.status()
        );
    }
}
