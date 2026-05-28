package com.wanted.momocity.friend.application.service;

import com.wanted.momocity.friend.application.command.CancelRequestFriendCommand;
import com.wanted.momocity.friend.application.usecase.CancelRequestFriendCommandUseCase;
import com.wanted.momocity.friend.domain.event.CancelRequestFriendPublishedEvent;
import com.wanted.momocity.friend.domain.repository.FriendRepository;
import com.wanted.momocity.friend.fmexception.FMResourceAccessDeniedException;
import com.wanted.momocity.friend.fmexception.FMResourceConflictException;
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
public class CancelRequestFriendCommandCommandService implements CancelRequestFriendCommandUseCase {

    private final FriendRepository friendRepository;
    //notification에 추가된 행 삭제를 위한 이벤트 발행자
    private final ApplicationEventPublisher eventPublisher;

    @Override
    public CancelRequestFriendView handle(CancelRequestFriendCommand command) {
        log.info("[CancelRequestFriendService] 친구 요청 철회 로직 시작 - 요청자: {}, 대상자: {}", command.userId(), command.targetUserId());

        //철회 대상자 유저가 실제로 존재하는지 확인(404)
        UserWithFMJpaEntity targetUser = friendRepository.findUserById(command.targetUserId())
                .orElseThrow(() -> new FMResourceNotFoundException("존재하지 않는 사용자입니다."));

        //두 사람 사이의 친구 관계 행 조회
        Optional<FriendJpaEntity> relationOpt = friendRepository.findRelationBetween(command.userId(), command.targetUserId());

        //404 사용자 없음(두 사람 사이에 아무런 요청 내역이 없을 때)
        if (relationOpt.isEmpty()) {
            log.warn("[CancelRequestFriendService] 철회 실패 - 요청 내역이 존재하지 않음");
            throw new FMResourceNotFoundException("철회할 요청 내역이 존재하지 않습니다.");
        }

        //relationOpt가 비어있지 않다는 걸 확인했으므로 주머니 속 진짜 객체를 꺼내 변수에 담기
        FriendJpaEntity relation = relationOpt.get();

        //403 권한 없음(내가 보낸 요청이 아닐 때)
        //기존 행의 fromUserId가 로그인한 유저가 아닐 때
        if (!relation.getFromUserId().getId().equals(command.userId())) {
            log.warn("[CancelRequestFriendService] 철회 실패 - 본인의 요청이 아님");
            throw new FMResourceAccessDeniedException("본인의 요청만 철회할 수 있습니다.");
        }

        //409 상태 모순(이미 수락 or 거절)
        //오직 SENT일 때만 철회 가능
        if (!"SENT".equals(relation.getStatus())) {
            log.warn("[CancelRequestFriendService] 철회 실패 - 이미 대기 상태가 아님 (현재 상태: {})", relation.getStatus());
            throw new FMResourceConflictException("이미 수락되거나 거절된 요청입니다. 취소할 수 없습니다.");
        }

        //friend 테이블에서 해당하는 행 삭제
        friendRepository.delete(relation);
        log.info("[CancelRequestFriendService] friend 테이블에서 요청 행 삭제 완료");

        //notification에 들어간 행 삭제를 위한 이벤트 발행
        UserWithFMJpaEntity loginUser = friendRepository.findUserById(command.userId()).orElseThrow();
        eventPublisher.publishEvent(new CancelRequestFriendPublishedEvent(relation.getId()));

        //응답 주머니 조립하여 컨트롤러로 반환
        return new CancelRequestFriendView(
                targetUser.getId(),
                targetUser.getNickname(),
                targetUser.getRole(),
                "none"
        );
    }
}
