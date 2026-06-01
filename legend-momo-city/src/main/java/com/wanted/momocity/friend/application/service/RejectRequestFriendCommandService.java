package com.wanted.momocity.friend.application.service;

import com.wanted.momocity.friend.application.command.RejectRequestFriendCommand;
import com.wanted.momocity.friend.application.policy.FriendEligibilityPolicy;
import com.wanted.momocity.friend.application.usecase.RejectRequestFriendCommandUseCase;
import com.wanted.momocity.friend.domain.repository.FriendRepository;
import com.wanted.momocity.friend.fmexception.FMResourceAccessDeniedException;
import com.wanted.momocity.friend.fmexception.FMResourceNotFoundException;
import com.wanted.momocity.friend.infrastructure.persistence.FriendJpaEntity;
import com.wanted.momocity.friend.user.UserWithFMJpaEntity;
import com.wanted.momocity.global.domain.common.exception.DomainRuleViolationException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional
public class RejectRequestFriendCommandService implements RejectRequestFriendCommandUseCase {

    private final FriendRepository friendRepository;
    private final FriendEligibilityPolicy eligibilityPolicy;

    //친구 요청 거절
    @Override
    public RejectView handle(RejectRequestFriendCommand command) {
        log.info("[RejectRequestFriendCommandService] 친구 요청 거절 로직 시작 - 거절자(로그인 유저): {}, 요청자(상대방): {}", command.userId(), command.fromUserId());

        //요청자(상대방) 검증
        UserWithFMJpaEntity fromUser = friendRepository.findUserById(command.fromUserId())
                .orElseThrow(() -> new FMResourceNotFoundException("존재하지 않는 사용자입니다."));

        //두 사람 사이의 관계 조회
        Optional<FriendJpaEntity> relationOpt = friendRepository.findRelationBetween(command.fromUserId(), command.userId());

        //404/409 검증 정책 위임
        eligibilityPolicy.ensureRejectable(relationOpt);

        FriendJpaEntity relation = relationOpt.get();

        //403(나에게 온 요청이 아닌 경우)
        if (!relation.getToUserId().getId().equals(command.userId())) {
            log.warn("[RejectRequestFriendCommandService] 거절 실패 - 본인에게 온 요청이 아님");
            throw new FMResourceAccessDeniedException("본인에게 온 요청만 거절할 수 있습니다.");
        }

        //거절 시 관계 행 완전 삭제
        Long targetFriendId = relation.getId();
        friendRepository.delete(relation);
        log.info("[RejectRequestFriendCommandService] friend 테이블 행 삭제 완료");

        //none 세팅 후 반환
        return new RejectView(
                fromUser.getId(),
                fromUser.getNickname(),
                fromUser.getRole(),
                "none"
        );
    }
}
