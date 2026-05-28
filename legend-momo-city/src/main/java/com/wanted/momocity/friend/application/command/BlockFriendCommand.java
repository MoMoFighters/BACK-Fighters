package com.wanted.momocity.friend.application.command;

public record BlockFriendCommand(
        Long userId, //차단하는 주체
        Long targetUserId //차단 당하는 사람
) {
}
