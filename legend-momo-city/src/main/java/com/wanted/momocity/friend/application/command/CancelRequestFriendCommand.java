package com.wanted.momocity.friend.application.command;

public record CancelRequestFriendCommand(
        Long userId, //로그인한 유저(요청을 철회하려는 주체)
        Long targetUserId
) {
}
