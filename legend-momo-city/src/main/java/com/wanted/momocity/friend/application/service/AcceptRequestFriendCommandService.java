package com.wanted.momocity.friend.application.service;

import com.wanted.momocity.friend.application.command.AcceptRequestFriendCommand;
import com.wanted.momocity.friend.application.policy.FriendEligibilityPolicy;
import com.wanted.momocity.friend.application.usecase.AcceptRequestFriendCommandUseCase;
import com.wanted.momocity.friend.domain.event.AcceptRequestFriendPublishedEvent;
import com.wanted.momocity.friend.domain.repository.FriendRepository;
import com.wanted.momocity.friend.fmexception.FMResourceAccessDeniedException;
import com.wanted.momocity.friend.fmexception.FMResourceNotFoundException;
import com.wanted.momocity.friend.infrastructure.persistence.FriendJpaEntity;
import com.wanted.momocity.friend.user.UserWithFMJpaEntity;
import com.wanted.momocity.global.domain.common.exception.DomainRuleViolationException;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional
public class AcceptRequestFriendCommandService implements AcceptRequestFriendCommandUseCase {

    private final FriendRepository friendRepository;
    //친구 수락 완료 시 이벤트 발행(알림 행 추가)
    private final ApplicationEventPublisher eventPublisher;
    //정책 주입
    private final FriendEligibilityPolicy eligibilityPolicy;

    //친구 요청 수락
    @Override
    public AcceptView handle(AcceptRequestFriendCommand command) {
        log.info("[AcceptRequestFriendCommandService] 친구 요청 수락 로직 시작 - 수락자(로그인 유저): {}, 요청자(상대방): {}", command.userId(), command.fromUserId());

        //요청자가 존재하는지 검증(404)
        UserWithFMJpaEntity fromUser = friendRepository.findUserById(command.fromUserId())
                .orElseThrow(() -> new FMResourceNotFoundException("존재하지 않는 사용자입니다."));

        //두 사람 사이의 친구 관계 조회(from/to 방향 확인)
        Optional<FriendJpaEntity> relationOpt = friendRepository.findRelationBetween(command.fromUserId(), command.userId());

        //검증은 policy에게 전달 위임(409 대응)
        eligibilityPolicy.ensureAcceptable(relationOpt);

        FriendJpaEntity relation = relationOpt.get();

        //403 권한 없음(나에게 온 요청이 아닐 때)
        //행이 toUserId가 현재 로그인한 사용자가 아니라면 수락 권한 없음
        if (!relation.getToUserId().getId().equals(command.userId())) {
            log.warn("[AcceptRequestFriendCommandService] 수락 실패 - 본인에게 온 요청이 아님");
            throw new FMResourceAccessDeniedException("본인에게 온 요청만 수락할 수 있습니다.");
        }

        //상태 전이: SENT -> FRIEND 상태 업데이트
        relation.changeStatus("FRIEND");
        log.info("[AcceptRequestFriendCommandService] 행 상태 변경 완료 (SENT -> FRIEND)");

        //로그인 유저 정보 로드(404)
        UserWithFMJpaEntity loginUser = friendRepository.findUserById(command.userId())
                .orElseThrow(() -> new FMResourceNotFoundException("로그인 유저 정보를 찾을 수 없습니다."));

        //이벤트 발행
        eventPublisher.publishEvent(new AcceptRequestFriendPublishedEvent(
                loginUser.getId(),
                loginUser.getNickname(),
                relation.getId() //알림 내역 추적용
        ));
        log.info("[AcceptRequestFriendCommandService] 친구 수락 알림 유도 이벤트 발행 성공 - 수신 대상 유저ID: {}", fromUser.getId());

        //응답용 주머니 조립 후 반환
        return new AcceptView(
                fromUser.getId(),
                fromUser.getNickname(),
                fromUser.getRole(),
                relation.getStatus()
        );
    }
}
