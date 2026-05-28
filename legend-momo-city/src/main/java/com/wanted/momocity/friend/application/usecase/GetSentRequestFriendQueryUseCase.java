package com.wanted.momocity.friend.application.usecase;

import java.util.List;

public interface GetSentRequestFriendQueryUseCase {
    List<SentRequestView> handle(Long userId);

    record SentRequestView(
            Long userId,
            String nickname,
            String role,
            String status, //친구 테이블 상태(SENT)
            Boolean isNotActive, //user 테이블의 활성 상태가 아닌 것,
            String profileImageUrl
    ) {}
}
