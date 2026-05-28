package com.wanted.momocity.friend.application.usecase;

import java.util.List;

public interface GetReceivedRequestFriendQueryUseCase {
    List<ReceivedRequestView> handle(Long userId);

    record ReceivedRequestView(
            Long userId, //요청자(fromUserId)
            String nickname, //요청자 닉네임
            String role,
            String status, //무조건 SENT
            Boolean isNotActive, //활성 상태 아닌 것
            String profileImageUrl
    ) {}
}
