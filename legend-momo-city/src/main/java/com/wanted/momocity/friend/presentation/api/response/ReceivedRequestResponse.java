package com.wanted.momocity.friend.presentation.api.response;

import com.wanted.momocity.friend.application.usecase.GetReceivedRequestFriendQueryUseCase.ReceivedRequestView;

public record ReceivedRequestResponse(
        Long userId,
        String nickname,
        String role,
        String status,
        String profileImageUrl
) {
    public static ReceivedRequestResponse from(ReceivedRequestView view) {
        //비활성 유저 닉네임 가공
        String displayNickname = view.nickname();
        if (view.isNotActive()) {
            displayNickname += "(알 수 없음)";
        }

        return new ReceivedRequestResponse(
                view.userId(),
                displayNickname,
                view.role(),
                view.status(),
                view.profileImageUrl()
        );
    }
}
