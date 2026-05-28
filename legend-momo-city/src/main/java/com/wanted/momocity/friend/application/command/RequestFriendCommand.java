package com.wanted.momocity.friend.application.command;

public record RequestFriendCommand(
        Long userId, //로그인한 유저
        Long targetUserId //친구 요청 대상자
) {
}
