package com.wanted.momocity.message.application.service;

import com.wanted.momocity.friend.fmexception.FMBusinessRuleViolationException;
import com.wanted.momocity.friend.fmexception.FMResourceNotFoundException;
import com.wanted.momocity.friend.infrastructure.persistence.FriendJpaEntity;

import com.wanted.momocity.friend.user.UserWithFMJpaEntity;
import com.wanted.momocity.global.domain.common.exception.DomainRuleViolationException;
import com.wanted.momocity.message.application.policy.MessageEligibilityPolicy;
import com.wanted.momocity.message.application.usecase.CreateChatRoomCommandUseCase;
import com.wanted.momocity.message.domain.repository.MessageRepository;
import com.wanted.momocity.message.infrastructure.persistence.*;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.xml.transform.Result;
import java.util.List;
import java.util.Optional;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional
public class CreateChatRoomCommandService implements CreateChatRoomCommandUseCase {

    private final MessageSideUserRepository messageSideUserRepository;
    private final MessageSideFriendRepository messageSideFriendRepository;
    private final MessageRepository messageRepository;
    private final MessageEligibilityPolicy messageEligibilityPolicy;
    private final SpringDataMessageRepository springDataMessageRepository;
    private final SpringDataChatRoomMemberRepository springDataChatRoomMemberRepository;

    //채팅방 조회 및 개설
    @Override
    public CreateRoomView handle(Long userId, Long targetUserId) {
        log.info("[CreateChatRoomCommandService] 채팅방 조회 및 개설 비즈니스 시작 - 요청자: {}, 대상자: {}", userId, targetUserId);

        //404(사용자 없음)
       UserWithFMJpaEntity targetUser = messageSideUserRepository.findById(targetUserId)
                .orElseThrow(() -> new FMResourceNotFoundException("존재하지 않는 사용자와의 대화창을 개설할 수 없습니다."));

        //두 사람 사이의 관계 양방향 조회
        String friendStatus = "none";
        Optional<FriendJpaEntity> relationOpt = messageSideFriendRepository.findByFromUserId_IdAndToUserId_Id(userId, targetUserId);
        if (relationOpt.isEmpty()) {
            relationOpt = messageSideFriendRepository.findByFromUserId_IdAndToUserId_Id(targetUserId, userId);
        }
        if (relationOpt.isPresent()) {
            friendStatus = relationOpt.get().getStatus();
        }

        //나와의 채팅 차단, 친구 상태 검증 위임(409)
        messageEligibilityPolicy.validateCreate(userId, targetUserId,friendStatus);

        Long finalRoomId = null;

        //1차 검증: 두 유저가 채팅방 멤버에 같이 있는 채팅방이 있는지 조회
        //어댑터 포트를 통해 두 유저가 있는 기존 채팅방이 존재하는지 검증
        Optional<Long> existingRoomIdOpt = messageRepository.findExistingRoom(userId, targetUserId);
        if (existingRoomIdOpt.isPresent()) {
            log.info("[CreateChatRoomCommandService] 1차 멤버 검증 성공 - 양방향 활성화된 채팅방 발견. 기존 방ID: {}", existingRoomIdOpt.get());
            finalRoomId = existingRoomIdOpt.get();
        } else {
            //2차 검증: 로그인한 사용자가 나갔을 때 혼자 남은 방 중 과거 대화 역추적
            log.info("[CreateChatRoomCommandService] 1차 검증 실패(나간 유저 존재) -> 2차 메시지 교차 검증 역추적 시작...");

            //상대방이 참여 중인 모든 멤버 다져옴
            List<ChatRoomMemberJpaEntity> targetMemberships = springDataChatRoomMemberRepository.findByUserId_Id(targetUserId);

            for (ChatRoomMemberJpaEntity membership : targetMemberships) {
                //내가 나간 방일 가능성 있는 후보방
                Long candidateRoomId = membership.getRoomId().getId();

                //상대방이 참여 중인 그 방의 인원이 혼자인지 확인
                List<ChatRoomMemberJpaEntity> roomMembers = springDataChatRoomMemberRepository.findByRoomId_Id(candidateRoomId);

                if (roomMembers.size() == 1) {
                    //상대방이 혼자 남은 방에 로그인 유저가 보낸 메시지가 1개라도 존재하는 지 확인
                    //로그인 유저가 보낸 메시지가 있다면 로그인 유저가 나간 방
                    boolean hasMyPastMessage = springDataMessageRepository.existsByRoomId_IdAndSenderId_Id(candidateRoomId, userId);

                    if (hasMyPastMessage) {
                        finalRoomId = candidateRoomId;
                        log.info("[CreatChatRoomCommandService] 2차 교차 검증 성공 - 로그인 유저가 나갔던 과거 채팅방 발견: {}", finalRoomId);

                        //로그인 유저가 나갔던 방이므로 해당 채팅방 멤버로 복구
                        UserWithFMJpaEntity loginUser = messageSideUserRepository.getReferenceById(userId);
                        ChatRoomJpaEntity existingRoom = messageRepository.findChatRoomById(finalRoomId)
                                .orElseThrow(() -> new FMBusinessRuleViolationException("존재하지 않는 채팅방입니다."));

                        ChatRoomMemberJpaEntity myNewMembership = ChatRoomMemberJpaEntity.createMembership(existingRoom, loginUser);
                        messageRepository.saveChatRoomMember(myNewMembership);
                        log.info("[CreateChatRoomCommandService] 나갔던 로그인 유저(ID: {})를 기존 방(ID: {})의 멤버로 복구 완료", userId, finalRoomId);

                        break;
                    }
                }
            }
        }

        //기존 방 찾았다면 리턴
        if (finalRoomId != null) {
            return new CreateRoomView(
                    true,
                    finalRoomId,
                    targetUser.getId(),
                    targetUser.getNickname(),
                    targetUser.getRole(),
                    "FRIEND"
            );
        }

        //기존 채팅방 없으면 신규 개설
        ChatRoomJpaEntity newRoom = new ChatRoomJpaEntity();
        messageRepository.saveChatRoom(newRoom);

        log.info("[CreateChatRoomCommandService] 신규 채팅방 멤버 저장 시작 - 방ID: {}, 요청자: {}, 대상자: {}",
                newRoom.getId(), userId, targetUserId);

        //로그인 유저 jpaEntity로 담기
        UserWithFMJpaEntity loginUser = messageSideUserRepository.getReferenceById(userId);

        //로그인 유저 멤버 저장
        ChatRoomMemberJpaEntity myMembership = ChatRoomMemberJpaEntity.createMembership(newRoom, loginUser);
        messageRepository.saveChatRoomMember(myMembership);
        //상대방 멤버 저장
        ChatRoomMemberJpaEntity targetMembership = ChatRoomMemberJpaEntity.createMembership(newRoom, targetUser);
        messageRepository.saveChatRoomMember(targetMembership);

        log.info("[CreateChatRoomCommandService] 신규 채팅방 개설 완료 - 방ID: {}", newRoom.getId());

        return new CreateRoomView(
                false,
                newRoom.getId(),
                targetUser.getId(),
                targetUser.getNickname(),
                targetUser.getRole(),
                "FRIEND"
        );
    }
}
