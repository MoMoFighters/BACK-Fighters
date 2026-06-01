package com.wanted.momocity.friend.application.service;

import com.wanted.momocity.friend.application.command.DeleteFriendCommand;
import com.wanted.momocity.friend.application.policy.FriendEligibilityPolicy;
import com.wanted.momocity.friend.application.usecase.DeleteFriendCommandUseCase;
import com.wanted.momocity.friend.domain.event.DeleteFriendPublishedEvent;
import com.wanted.momocity.friend.domain.repository.FriendRepository;
import com.wanted.momocity.friend.fmexception.FMResourceNotFoundException;
import com.wanted.momocity.friend.infrastructure.persistence.FriendJpaEntity;


import com.wanted.momocity.friend.user.UserWithFMJpaEntity;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional
public class DeleteFriendCommandService implements DeleteFriendCommandUseCase {

    private final FriendRepository friendRepository;
    private final FriendEligibilityPolicy friendEligibilityPolicy;
    //친구 삭제 시 채팅방 나가기 이벤트 발행
    private final ApplicationEventPublisher eventPublisher;

    //친구 삭제
    @Override
    public DeleteView handle(DeleteFriendCommand command) {
        log.info("[DeleteFriendCommandService] 친구 목록 삭제 시도 - 주체: {}, 대상: {}", command.userId(), command.targetUserId());

        //대상 유저 정보 가져오기(없으면 404)
        UserWithFMJpaEntity targetUser = friendRepository.findUserById(command.targetUserId())
                .orElseThrow(() -> new FMResourceNotFoundException("삭제할 친구 내역이 존재하지 않습니다."));
        //관련행 찾기
        Optional<FriendJpaEntity> relationOpt = friendRepository.findAnyRelationBetween(command.userId(), command.targetUserId());

        //정책 위임(404, 409)
        friendEligibilityPolicy.ensureDeletable(relationOpt, targetUser.getRole());

        FriendJpaEntity relation = relationOpt.get();
        friendRepository.delete(relation);
        log.info("[DeleteFriendCommandService] friend 테이블에서 행 삭제 완료 - 행ID: {}", relation.getId());

        //채팅방 나가기를 위한 이벤트 발행
        eventPublisher.publishEvent(new DeleteFriendPublishedEvent(
                command.userId(),
                command.targetUserId()
        ));
        log.info("[DeleteFriendCommandService] 친구 삭제 이벤트 발행 완료 - 요청자: {}, 대상자: {}", command.userId(), command.targetUserId());

        return new DeleteView(
                targetUser.getId(),
                targetUser.getNickname(),
                targetUser.getRole(),
                "none"
        );
    }
}
