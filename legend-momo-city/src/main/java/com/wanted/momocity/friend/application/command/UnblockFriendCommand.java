package com.wanted.momocity.friend.application.command;

public record UnblockFriendCommand(
        Long userId, //차단 해제하는 추제(로그인 유저)
        Long targetUserId //차단 해제 당하는 대상 유저
) {
}
