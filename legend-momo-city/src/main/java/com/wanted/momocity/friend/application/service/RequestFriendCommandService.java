package com.wanted.momocity.friend.application.service;

import com.wanted.momocity.friend.application.command.RequestFriendCommand;
import com.wanted.momocity.friend.application.policy.FriendEligibilityPolicy;
import com.wanted.momocity.friend.application.usecase.RequestFriendCommandUseCase;
import com.wanted.momocity.friend.domain.event.RequestFriendPublishedEvent;
import com.wanted.momocity.friend.domain.model.Friend;
import com.wanted.momocity.friend.domain.repository.FriendRepository;
import com.wanted.momocity.friend.fmexception.FMBusinessRuleViolationException;
import com.wanted.momocity.friend.fmexception.FMResourceNotFoundException;
import com.wanted.momocity.friend.infrastructure.persistence.FriendJpaEntity;
import com.wanted.momocity.friend.user.UserWithFMJpaEntity;
import com.wanted.momocity.global.domain.common.exception.DomainRuleViolationException;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class RequestFriendCommandService implements RequestFriendCommandUseCase {

    private final FriendRepository friendRepository;
    private final ApplicationEventPublisher eventPublisher; //스프링 이벤트 발행기
    //비즈니스 정책 주입
    private final FriendEligibilityPolicy eligibilityPolicy;
    //친구 요청
    @Override
    public RequestFriendView handle(RequestFriendCommand command) {
        log.info("[RequestFriendCommandService] 친구 요청 명령 수행 시작 - 요청자: {}, 대상자: {}", command.userId(), command.targetUserId());

        //순수 엔티티 조회 및 검증 로직(400, 404)
        UserWithFMJpaEntity targetUser = friendRepository.findUserById(command.targetUserId())
                .orElseThrow(() -> new FMResourceNotFoundException("존재하지 않는 사용자에게 요청을 보낼 수 없습니다."));
        log.info("[RequestFriendCommandService] 대상자 검증 완료 - 닉네임: '{}', 역할: '{}'",targetUser.getNickname(), targetUser.getRole());

        UserWithFMJpaEntity loginUser = friendRepository.findUserById(command.userId())
                .orElseThrow(() -> new FMResourceNotFoundException("로그인 유저 정보를 찾을 수 없습니다."));
        log.info("[RequestFriendCommandService] 요청자 검증 완료 - 닉네임: '{}'", loginUser.getNickname());

        //기존 관계 추출
        Optional<FriendJpaEntity> relation = friendRepository.findAnyRelationBetween(command.userId(), command.targetUserId());

        //검증은 policy에게 위임
        eligibilityPolicy.ensureEligible(command.userId(), command.targetUserId(), relation, targetUser.getRole());
        log.warn("[RequestFriendCommandService] 비즈니스 자격 검증(Policy) 통과 완료 - 친구 요청 진행");

        //순수 도메인 애그리거트를 먼저 탄생시킴
        Friend domainFriend = Friend.createRequest(loginUser.getId(), targetUser.getId());

        //friend 테이블에 저장하고 영속화되어 id가 발급된 객체를 변수에 받음
        FriendJpaEntity newRelation = FriendJpaEntity.createRequest(loginUser, targetUser);
        log.info("[RequestFriendCommandService] SENT 상태의 FriendJpaEntity 인스턴스 생성 완료");

        FriendJpaEntity savedRelation = friendRepository.saveFriendRelation(newRelation);
        log.info("[RequestFriendCommandService] friend 테이블 행 저장 완료 - 생성된 관계 식별 PK(ID): {}", savedRelation.getId());

        //알림 도메인을 위한 이벤트 발행
        eventPublisher.publishEvent(new RequestFriendPublishedEvent(
                loginUser.getId(),
                loginUser.getNickname(),
                targetUser.getId(),
                savedRelation.getId() //ref_id 용도로 friend_id 추가
        ));
        log.info("[RequestFriendCommandService] friend 행 추가 완료 및 비동기 알림 유도 이벤트 발행 성공(ref_id: {})", savedRelation.getId());

        //컨트롤러가 사용할 View 주머니 리턴
        return new RequestFriendView(
                targetUser.getId(),
                targetUser.getNickname(),
                domainFriend.getStatus().name(),
                targetUser.getRole()
        );
    }
}
