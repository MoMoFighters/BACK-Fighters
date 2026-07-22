package com.wanted.momocity.friend.presentation.api.response;

import com.wanted.momocity.friend.application.usecase.FriendQueryUseCase.SentRequestView;

public record SentRequestResponse(
        Long userId,
        String nickname,
        String role,
        String status,
        String profileImageUrl
) {
    public static SentRequestResponse from(SentRequestView view) {
        //ACTIVE 아니면 (알 수 없음) 가공
        //비활성 유저 닉네임 가공
        String displayNickname = view.nickname();
        //user 담당자가 가공해서 주석처리함

        return new SentRequestResponse(
                view.userId(),
                displayNickname,
                view.role(),
                view.status(),
                view.profileImageUrl()
        );
    }
}
