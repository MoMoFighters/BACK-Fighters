package com.wanted.momocity.message.application.query;

public record GetChatMemberListQuery(
        Long roomId,
        Long userId
) {
}
