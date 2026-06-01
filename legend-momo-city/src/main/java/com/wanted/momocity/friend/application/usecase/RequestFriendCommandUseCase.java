package com.wanted.momocity.friend.application.usecase;

import com.wanted.momocity.friend.application.command.RequestFriendCommand;

public interface RequestFriendCommandUseCase {

    //입력 모델로 Command를 받고 출력 모델로 내부 주머니(View)를 반환.
    RequestFriendView handle(RequestFriendCommand command);

    //컨트롤러에 최종 전달할 결과
    record RequestFriendView(
            Long userId,
            String nickname,
            String status,
            String role
    ) {
    }
}
