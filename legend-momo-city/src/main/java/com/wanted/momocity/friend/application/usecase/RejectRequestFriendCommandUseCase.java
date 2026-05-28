package com.wanted.momocity.friend.application.usecase;

import com.wanted.momocity.friend.application.command.RejectRequestFriendCommand;

public interface RejectRequestFriendCommandUseCase {
    RejectView handle(RejectRequestFriendCommand command);

    record RejectView(
            Long userId, //친구 테이블의 fromUserId(요청자)
            String nickname, //요청자 닉네임
            String role,
            String status //none으로 반환
    ) {}
}
