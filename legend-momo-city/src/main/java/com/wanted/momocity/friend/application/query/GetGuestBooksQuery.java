package com.wanted.momocity.friend.application.query;

public record GetGuestBooksQuery(
        Long userId,
        Long cityOwnerId
) {
}
