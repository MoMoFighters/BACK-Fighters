package com.wanted.momocity.friend.presentation.api.response;

import com.wanted.momocity.friend.application.usecase.DeleteFriendCommandUseCase.DeleteView;

public record DeleteFriendResponse(
        Long userId,
        String nickname,
        String role,
        String status
) {
    public static DeleteFriendResponse from(DeleteView view) {
        return new DeleteFriendResponse(
                view.userId(),
                view.nickname(),
                view.role(),
                view.status()
        );
    }
}
