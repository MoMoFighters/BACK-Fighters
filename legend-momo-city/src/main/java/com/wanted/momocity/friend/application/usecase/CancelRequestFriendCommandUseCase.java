package com.wanted.momocity.friend.application.usecase;

import com.wanted.momocity.friend.application.command.CancelRequestFriendCommand;

public interface CancelRequestFriendCommandUseCase {

    CancelRequestFriendView handle(CancelRequestFriendCommand command);

    //컨트롤러에 데이터를 넘겨줄 내부 주머니
    record CancelRequestFriendView(
            Long userId,
            String nickname,
            String role,
            String status
    ) {

    }
}
