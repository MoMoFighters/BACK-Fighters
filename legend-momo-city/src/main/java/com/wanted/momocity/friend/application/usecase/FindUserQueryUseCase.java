package com.wanted.momocity.friend.application.usecase;

import java.util.List;

public interface FindUserQueryUseCase {

    List<FindView> handle(Long userId, String nickname);

    record FindView(
            Long userId,
            String name,
            String nickname,
            String status,
            String role,
            Boolean isNotActive, //user 테이블의 활성 상태가 아닌 것
            List<String> lectureTitle,
            String profileImageUrl
    ) {}
}
