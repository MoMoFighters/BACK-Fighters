package com.wanted.momocity.friend.application.command;

public record RejectRequestFriendCommand(
        Long userId, //로그인 유저(거절 버튼 누르는 주체)
        Long fromUserId //요청 보냈던 사람
) {
}
