package com.wanted.momocity.friend.application.service;

import com.wanted.momocity.friend.application.command.UnblockFriendCommand;
import com.wanted.momocity.friend.application.policy.FriendEligibilityPolicy;
import com.wanted.momocity.friend.application.usecase.UnblockFriendCommandUseCase;
import com.wanted.momocity.friend.domain.repository.FriendRepository;
import com.wanted.momocity.friend.infrastructure.persistence.FriendJpaEntity;

import com.wanted.momocity.friend.user.UserWithFMJpaEntity;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional
public class UnblockFriendCommandService implements UnblockFriendCommandUseCase {

    private final FriendRepository friendRepository;
    private final FriendEligibilityPolicy friendEligibilityPolicy;

    //친구 차단 해제
    @Override
    public UnblockView handle(UnblockFriendCommand command) {
        log.info("[UnblockFriendCommandService] 친구 차단 해제 시도 - 주체: {}, 대상: {}", command.userId(), command.targetUserId());

        //양방향 단건 조회로 찾기
        Optional<FriendJpaEntity> relationOpt = friendRepository.findAnyRelationBetween(command.userId(), command.targetUserId());

        //정책 레이어에 위임(없으면 404, 아니면 409)
        friendEligibilityPolicy.ensureUnblockable(relationOpt);

        FriendJpaEntity relation = relationOpt.get();

        //다시 FRIEND로 되돌림(더티 체킹)
        relation.changeStatus("FRIEND");
        log.info("[UnblockFriendCommandService] BLOCK -> FRIEND 상태 원복 완료 - 행ID: {}", relation.getId());

        //행 방향 상관없이 상대방 유저 정보 가져오기
        UserWithFMJpaEntity targetUser = (relation.getFromUserId().getId().equals(command.userId()))
                ? relation.getToUserId() : relation.getFromUserId();

        log.info("[UnblockFriendCommandService] 최종 친구 차단 해제 완료 - 대상 닉네임: {}, 상태: {}", targetUser.getNickname(), relation.getStatus());
        return new UnblockView(
                targetUser.getId(),
                targetUser.getNickname(),
                targetUser.getRole(),
                relation.getStatus()
        );
    }
}
