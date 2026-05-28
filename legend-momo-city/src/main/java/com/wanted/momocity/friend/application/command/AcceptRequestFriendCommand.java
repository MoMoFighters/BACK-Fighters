package com.wanted.momocity.friend.application.command;

public record AcceptRequestFriendCommand(
        Long userId, //로그인 유저
        Long fromUserId //요청을 보냈던 사람
) {
}
