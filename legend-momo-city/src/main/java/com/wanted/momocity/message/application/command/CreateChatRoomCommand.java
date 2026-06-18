package com.wanted.momocity.message.application.command;

import java.util.List;

public record CreateChatRoomCommand(
        Long userId, //로그인 유저
        String roomTitle,
        List<Long> chatMembers //개설 대상자
) {
}
