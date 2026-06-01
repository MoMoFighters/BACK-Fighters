package com.wanted.momocity.friend.application.service;

import com.wanted.momocity.friend.application.usecase.GetBlockedFriendQueryUseCase;
import com.wanted.momocity.friend.domain.repository.FriendRepository;
import com.wanted.momocity.friend.infrastructure.persistence.FriendJpaEntity;

import com.wanted.momocity.friend.user.UserWithFMJpaEntity;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class GetBlockedFriendQueryService implements GetBlockedFriendQueryUseCase {

    private final FriendRepository friendRepository;

    //친구 차단한 목록
    @Override
    public List<BlockedView> handle(Long userId) {
        log.info("[GetBlockedFriendQueryService] 내가 차단한 유저 목록 조회 시작 - 유저ID: {}", userId);

        //로그인 유저와 연관된 모든 관계 행 가져오기
        List<FriendJpaEntity> allRelations = friendRepository.findAllMyRelations(userId);
        List<BlockedView> blockedViews = new ArrayList<>();

        for (FriendJpaEntity relation : allRelations) {
            //BLOCK 상태만 가져오기
            if (!"BLOCK".equals(relation.getStatus())) {
                continue;
            }

            //방어막(로그인 유저가 toUser인 상태에서 BLOCK일 때만 띄움)
            if (relation.getToUserId().getId().equals(userId)) {
                log.info("[GetBlockedFriendQueryService] 상대방이 나를 차단한 행이므로 노출 제외 - 관계ID: {}", relation.getId());
                continue;
            }

            //로그인한 유저가 차단한 상대방 누구인지.
            UserWithFMJpaEntity targetUser;

            //로그인 유저가 From이면 상대는 To, 로그인 유저가 To면 상대가 From
            if (relation.getFromUserId().getId().equals(userId)) {
                targetUser = relation.getToUserId();
            } else {
                targetUser = relation.getFromUserId();
            }

            blockedViews.add(new BlockedView(
                    targetUser.getId(),
                    targetUser.getNickname(),
                    targetUser.getRole(),
                    relation.getStatus(),
                    !"ACTIVE".equals(targetUser.getStatus()),
                    targetUser.getProfileImageUrl()
            ));
        }

        log.info("[GetBlockedFriendQueryService] 내가 차단한 유저 목록 조회 완료 - 총 {}명", blockedViews.size());
        return blockedViews;
    }
}
