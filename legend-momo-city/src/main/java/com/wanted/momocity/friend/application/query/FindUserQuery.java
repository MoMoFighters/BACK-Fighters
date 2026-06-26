package com.wanted.momocity.friend.application.query;

public record FindUserQuery(
        Long userId,
        String findNickname
) {
}
