package com.wanted.momocity.friend.presentation.api.response;

import com.wanted.momocity.friend.application.usecase.RequestFriendCommandUseCase.RequestFriendView;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "친구 요청 성공 시 응답 객체")
public record RequestFriendResponse(
        Long userId,
        String nickname,
        String status,
        String role
) {
    public static RequestFriendResponse from(RequestFriendView view) {
        return new RequestFriendResponse(
                view.userId(),
                view.nickname(),
                view.status(),
                view.role()
        );
    }
}
