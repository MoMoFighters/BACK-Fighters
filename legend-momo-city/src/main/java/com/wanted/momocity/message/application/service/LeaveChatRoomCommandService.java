package com.wanted.momocity.message.application.service;

import com.wanted.momocity.friend.fmexception.FMResourceNotFoundException;
import com.wanted.momocity.friend.infrastructure.persistence.FriendJpaEntity;
import com.wanted.momocity.friend.user.UserWithFMJpaEntity;
import com.wanted.momocity.message.application.command.LeaveChatRoomCommand;
import com.wanted.momocity.message.application.policy.MessageEligibilityPolicy;
import com.wanted.momocity.message.application.usecase.LeaveChatRoomCommandUseCase;
import com.wanted.momocity.message.domain.repository.MessageRepository;
import com.wanted.momocity.message.infrastructure.persistence.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional
public class LeaveChatRoomCommandService implements LeaveChatRoomCommandUseCase {
    private final SpringDataChatRoomRepository springDataChatRoomRepository;
    private final SpringDataChatRoomMemberRepository springDataChatRoomMemberRepository;
    private final MessageEligibilityPolicy messageEligibilityPolicy;
    private final SpringDataMessageRepository springDataMessageRepository;
    private final MessageRepository messageRepository;
    private final MessageSideUserRepository messageSideUserRepository;
    private final MessageSideFriendRepository messageSideFriendRepository;

    //채팅방 나가기
    @Override
    public LeaveChatRoomView handle(Long roomId, Long userId) {
        log.info("[LeaveChatRoomCommandService] 채팅방 나가기 검증 시작 - 방ID: {}, 유저ID: {}", roomId, userId);

        //나가려는 채팅방이 존재하는 지 검증(404)
        ChatRoomJpaEntity chatRoom = springDataChatRoomRepository.findById(roomId)
                .orElseThrow(() -> new FMResourceNotFoundException("존재하지 않거나 이미 나간 채팅방입니다."));

        //해당 방에 들어있는 모든 멤버 가져오기
        List<ChatRoomMemberJpaEntity> allMembers = springDataChatRoomMemberRepository.findByRoomId_Id(roomId);

        //정책 위임(403, 409)
        messageEligibilityPolicy.leaveChatRoom(userId, roomId, allMembers);

        //로그인 유저 정보 추출
        ChatRoomMemberJpaEntity myMembership = null;
        for (ChatRoomMemberJpaEntity member: allMembers) {
            if (member.getUserId().getId().equals(userId)) {
                myMembership = member;
                break;
            }
        }

        //방 분기
        int currentMemberCount = allMembers.size();

        //로그인 유저가 방에 남은 마지막 사용자일 때(chat_room, chat_room_member, message 모두 삭제)
        if (currentMemberCount <= 1) {
            log.info("[LeaveChatRoomCommandService] 마지막 사용자 퇴장 처리 -> 방 폭파 진행");
            springDataMessageRepository.deleteByRoomId_Id(roomId);
            springDataChatRoomMemberRepository.delete(myMembership);
            springDataChatRoomRepository.delete(chatRoom);

            return new LeaveChatRoomView(
                    true,
                    roomId,
                    null,
                    null,
                    null,
                    null
            );
        }

        //상대방이 남아있을 때(chat_room_member에서만 삭제)
        log.info("[LeaveChatRoomCommandService] 상대방 존재 확인 -> 로그인 유저 멤버 행만 삭제");
        springDataChatRoomMemberRepository.delete(myMembership);

        // 🎯 [수정]: 전체 멤버 중 '내가 아닌 사람(남겨진 사람)'을 정확히 찾아옵니다.
        // 이러면 나중에 다대다로 확장되어도 최소한 남은 사람 중 한 명을 안전하게 찝어올 수 있습니다.
        UserWithFMJpaEntity targetUser = null;
        for (ChatRoomMemberJpaEntity member : allMembers) {
            if (!member.getUserId().getId().equals(userId)) {
                targetUser = member.getUserId();
                break;
            }
        }

        String friendStatus = "none";
        Optional<FriendJpaEntity> relationOpt = messageSideFriendRepository.findByFromUserId_IdAndToUserId_Id(userId, targetUser.getId());
        if (relationOpt.isEmpty()) {
            relationOpt = messageSideFriendRepository.findByFromUserId_IdAndToUserId_Id(targetUser.getId(), userId);
        }
        if (relationOpt.isPresent()) {
            friendStatus = relationOpt.get().getStatus();
        }

        return new LeaveChatRoomView(
                false,
                roomId,
                targetUser.getId(),
                targetUser.getNickname(),
                targetUser.getRole(),
                friendStatus
        );
    }
}

