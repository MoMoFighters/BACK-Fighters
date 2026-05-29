package com.wanted.momocity.friend.application.usecase;

import java.util.List;

public interface GetBlockedFriendQueryUseCase {
    List<BlockedView> handle(Long userId);

    record BlockedView(
            Long userId,
            String nickname,
            String role,
            String status,
            Boolean isNotActive,
            String profileImageUrl
    ) {}
}
