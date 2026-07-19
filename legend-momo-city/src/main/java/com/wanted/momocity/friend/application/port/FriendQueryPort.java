package com.wanted.momocity.friend.application.port;

public interface FriendQueryPort {
    boolean isFriend(Long userId1, Long userId2);
}