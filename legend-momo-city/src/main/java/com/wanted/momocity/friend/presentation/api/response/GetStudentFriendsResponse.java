package com.wanted.momocity.friend.presentation.api.response;

import com.wanted.momocity.friend.application.usecase.FriendQueryUseCase.StudentFriendsView;

public record GetStudentFriendsResponse(
        Long userId,
        String nickname,
        String role, //모두 STUDENT일 것
        String status,
        String profileImageUrl
) {

    public static GetStudentFriendsResponse from(StudentFriendsView view) {
        return new GetStudentFriendsResponse(
                view.userId(),
                view.nickname(),
                view.role(),
                view.status(),
                view.profileImageUrl()
        );
    }

}
