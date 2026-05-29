package com.wanted.momocity.friend.application.service;

import com.wanted.momocity.friend.application.command.BlockFriendCommand;
import com.wanted.momocity.friend.application.policy.FriendEligibilityPolicy;
import com.wanted.momocity.friend.application.usecase.BlockFriendCommandUseCase;
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
public class BlockFriendCommandService implements BlockFriendCommandUseCase {

    private final FriendRepository friendRepository;
    private final FriendEligibilityPolicy friendEligibilityPolicy;

    //친구 차단
    @Override
    public BlockView handle(BlockFriendCommand command) {
        log.info("[BlockFriendCommandService] 친구 차단 시도 - 차단 주체(로그인 유저): {}, 차단 대상: {}", command.userId(), command.targetUserId());

        //두 사람 사이의 관계 존재 확인
        Optional<FriendJpaEntity> relationOpt = friendRepository.findAnyRelationBetween(command.userId(), command.targetUserId());

        //차단 대상자 유저 객체 담기(행의 방향과 상관없이 '로그인한 유저'가 아닌 상대방 유저의 정보 추출
        String targetRole = "STUDENT";
        UserWithFMJpaEntity targetUser = null;

        if (relationOpt.isPresent()) {
            //검증 통과했으므로 무조건 행 존재
            FriendJpaEntity relation = relationOpt.get();
            targetUser = (relation.getFromUserId().getId().equals(command.userId()))
                    ? relation.getToUserId() : relation.getFromUserId();
            targetRole = targetUser.getRole();
        }
        //FRIEND일 때만 통과
        friendEligibilityPolicy.ensureBlockable(relationOpt,  targetRole);

        FriendJpaEntity relation = relationOpt.get();
        String finalStatus = "BLOCK";

        if (relation.getToUserId().getId().equals(command.userId())) {
            log.info("[BlockFriendCommandService] 로그인 유저가 To이므로 행의 방향을 바꾸고 BLOCK 처리 - 행ID: {}", relation.getId());
            relation.swapDirectionAndBlock();
        } else {
            //상태를 BLOCK으로 변경
            relation.changeStatus("BLOCK");
            log.info("[BlockFriendCommandService] BLOCK으로 변경 완료 - 행ID: {}", relation.getId());
        }


        log.info("[BlockFriendCommandService] 최종 친구 차단 완료 - 대상 닉네임: {}, 상태: {}", targetUser.getNickname(), relation.getStatus());

        //뷰 주머니에 담아서 리턴
        return new BlockView(
                targetUser.getId(),
                targetUser.getNickname(),
                targetUser.getRole(),
                relation.getStatus()
        );
    }
}
