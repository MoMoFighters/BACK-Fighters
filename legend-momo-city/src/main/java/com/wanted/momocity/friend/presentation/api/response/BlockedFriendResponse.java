package com.wanted.momocity.friend.presentation.api.response;

import com.wanted.momocity.friend.application.usecase.GetBlockedFriendQueryUseCase.BlockedView;

public record BlockedFriendResponse(
        Long userId,
        String nickname,
        String role,
        String status,
        String profileImageUrl
) {
    public static BlockedFriendResponse from(BlockedView view) {
        //ACTIVE가 아니면 (알 수 없음)
        String displayNickname = view.nickname();
        if (view.isNotActive()) {
            displayNickname += "(알 수 없음)";
        }

        return new BlockedFriendResponse(
                view.userId(),
                displayNickname,
                view.role(),
                view.status(),
                view.profileImageUrl()
        );
    }
}
