package com.wanted.momocity.message.application.command;

public record ReadMessageCommand(
        Long roomId,
        Long userId //로그인 유저
) {
}
