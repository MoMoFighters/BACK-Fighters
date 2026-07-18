package com.wanted.momocity.friend.infrastructure.adapter;

import com.wanted.momocity.friend.domain.repository.FriendRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

// study 관련 친구 관계 검증

@Component
@RequiredArgsConstructor
public class FriendQueryAdapter implements FriendQueryPort {
    private final FriendRepository friendRepository;

    @Override
    public boolean isFriend(Long userId1, Long userId2) {
        return friendRepository.findAnyRelationBetween(userId1, userId2)
                .map(f -> "FRIEND".equals(f.getStatus()))
                .orElse(false);
    }
}
