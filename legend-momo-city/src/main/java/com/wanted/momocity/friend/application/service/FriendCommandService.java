package com.wanted.momocity.friend.application.service;

import com.wanted.momocity.friend.application.command.*;
import com.wanted.momocity.friend.application.policy.FriendEligibilityPolicy;
import com.wanted.momocity.friend.application.usecase.FriendCommandUseCase;
import com.wanted.momocity.friend.domain.event.AcceptRequestFriendPublishedEvent;
import com.wanted.momocity.friend.domain.event.CancelRequestFriendPublishedEvent;
import com.wanted.momocity.friend.domain.event.DeleteFriendPublishedEvent;
import com.wanted.momocity.friend.domain.event.RequestFriendPublishedEvent;
import com.wanted.momocity.friend.domain.model.Friend;
import com.wanted.momocity.friend.domain.repository.FriendRepository;
import com.wanted.momocity.friend.fmexception.FMResourceAccessDeniedException;
import com.wanted.momocity.friend.fmexception.FMResourceConflictException;
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
@Transactional
@RequiredArgsConstructor
@Slf4j
public class FriendCommandService implements FriendCommandUseCase {

    private final FriendRepository friendRepository;
    private final ApplicationEventPublisher eventPublisher; //스프링 이벤트 발행기
    //비즈니스 정책 주입
    private final FriendEligibilityPolicy friendEligibilityPolicy;

    //친구 요청
    @Override
    public RequestFriendView requestFriendCommandHandle(RequestFriendCommand command) {
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
        friendEligibilityPolicy.ensureEligible(command.userId(), command.targetUserId(), relation, targetUser.getRole());
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

    //친구 요청 철회
    @Override
    public CancelRequestFriendView cancelRequestFriendCommandHandle(CancelRequestFriendCommand command) {
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

    //친구 요청 수락
    @Override
    public AcceptView acceptRequestFriendCommandHandle(AcceptRequestFriendCommand command) {
        log.info("[AcceptRequestFriendCommandService] 친구 요청 수락 로직 시작 - 수락자(로그인 유저): {}, 요청자(상대방): {}", command.userId(), command.fromUserId());

        //요청자가 존재하는지 검증(404)
        UserWithFMJpaEntity fromUser = friendRepository.findUserById(command.fromUserId())
                .orElseThrow(() -> new FMResourceNotFoundException("존재하지 않는 사용자입니다."));

        //두 사람 사이의 친구 관계 조회(from/to 방향 확인)
        Optional<FriendJpaEntity> relationOpt = friendRepository.findRelationBetween(command.fromUserId(), command.userId());

        //검증은 policy에게 전달 위임(409 대응)
        friendEligibilityPolicy.ensureAcceptable(relationOpt);

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

    //친구 요청 거절
    @Override
    public RejectView rejectRequestFriendCommandHandle(RejectRequestFriendCommand command) {
        log.info("[RejectRequestFriendCommandService] 친구 요청 거절 로직 시작 - 거절자(로그인 유저): {}, 요청자(상대방): {}", command.userId(), command.fromUserId());

        //요청자(상대방) 검증
        UserWithFMJpaEntity fromUser = friendRepository.findUserById(command.fromUserId())
                .orElseThrow(() -> new FMResourceNotFoundException("존재하지 않는 사용자입니다."));

        //두 사람 사이의 관계 조회
        Optional<FriendJpaEntity> relationOpt = friendRepository.findRelationBetween(command.fromUserId(), command.userId());

        //404/409 검증 정책 위임
        friendEligibilityPolicy.ensureRejectable(relationOpt);

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

    //친구 차단
    @Override
    public BlockView blockFriendCommandHandle(BlockFriendCommand command) {
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

    //친구 차단 해제
    @Override
    public UnblockView unblockFriendCommandHandle(UnblockFriendCommand command) {
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

    //친구 삭제
    @Override
    public DeleteView deleteFriendCommandHandle(DeleteFriendCommand command) {
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
