package com.wanted.momocity.message.application.command;

public record CreateChatRoomCommand(
        Long userId, //로그인 유저
        Long targetUserId //개설 대상자
) {
}
