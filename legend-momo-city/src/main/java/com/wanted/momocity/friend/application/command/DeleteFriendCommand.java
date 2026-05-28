package com.wanted.momocity.friend.application.command;

public record DeleteFriendCommand(
        Long userId, //삭제 요청한 주체(로그인 유저)
        Long targetUserId
) {
}
