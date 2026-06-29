package com.wanted.momocity.friend.application.command;

public record RegisterGuestBookCommand(
        Long ownerId, //도시 주인
        Long userId, //로그인 유저
        String content
) {
}
