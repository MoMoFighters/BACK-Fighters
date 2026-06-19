package com.wanted.momocity.message.application.usecase;

import com.wanted.momocity.message.application.command.CreateChatRoomCommand;
import com.wanted.momocity.message.application.command.SendMessageCommand;

import java.time.LocalDateTime;
import java.util.List;

public interface MessageCommandUseCase {

    //채팅방 조회 및 개설
    CreateRoomView createChatRoomCommandHandle(CreateChatRoomCommand command);

    record CreateRoomView(
            boolean isExisting, //기존 방 존재 여부
            RoomInfo roomInfo,
            List<MemberInfo> memberInfo
    ) {}
    record RoomInfo(
            Long roomId,
            String roomTitle,
            Long inMemberCount
    ) {}
    record MemberInfo(
            Long userId,
            String name,
            String nickname,
            String role,
            String status
    ) {}

    //메시지 전송
    SendView sendMessageCommandHandle(SendMessageCommand command);

    record SendView(
            Long roomId,
            Long targetUserId,
            String targetNickname,
            String targetRole,
            String friendStatus,
            String content,
            LocalDateTime createdAt
    ) {}

    //메시지 읽음
    ReadView readMessageCommandHandle(Long roomId, Long userId);

    record ReadView(
            Long roomId,
            Long targetUserId,
            String nickname,
            boolean hasUnread //새로 읽은 게 있는지 여부
    ) {}

    //채팅방 나가기
    LeaveChatRoomView leaveChatRoomCommandHandle(Long roomId, Long userId);

    record LeaveChatRoomView(
            boolean isLastMember, //마지막 남은 사람이었는지 여부
            Long roomId,
            Long userId, //남겨진 사람 아이디
            String nickname, //남겨진 사람 닉네임
            String role,
            String status
    ) {}
}
