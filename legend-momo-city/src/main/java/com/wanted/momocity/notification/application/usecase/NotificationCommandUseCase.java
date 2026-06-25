package com.wanted.momocity.notification.application.usecase;

import com.wanted.momocity.notification.application.command.*;

import java.time.LocalDateTime;
import java.util.List;

public interface NotificationCommandUseCase {

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
    ReadView readMessageCommandHandle(ReadMessageCommand command);

    record ReadView(
            Long roomId,
            Long targetUserId,
            String nickname,
            boolean hasUnread //새로 읽은 게 있는지 여부
    ) {}

    //채팅방 나가기
    LeaveChatRoomView leaveChatRoomCommandHandle(LeaveChatRoomCommand command);

    record LeaveChatRoomView(
            boolean isLastMember, //마지막 남은 사람이었는지 여부
            Long roomId,
            Long userId, //남겨진 사람 아이디
            String nickname, //남겨진 사람 닉네임
            String role,
            String status
    ) {}

    //채팅방 이름 변경
    ModifyRoomTitleView modifyRoomTitleCommandHandle(ModifyRoomTitleCommand command);

    record ModifyRoomTitleView(
            Long roomId,
            Long userId,
            String nickname, //바꾼 주체 닉네임
            String role,
            String roomTitle,
            LocalDateTime createdAt
    ) {}

    //멤버 초대하기(다대다)
    InviteRoomMemberView inviteRoomMemberCommandHandle(InviteRoomMemberCommand command);

    record InviteRoomMemberView(
            Long roomId,
            String roomTitle,
            Long userId, //초대 주체
            String nickname, //초대 주체
            String role,
            LocalDateTime joinedAt, //초대한 시간
            String invitedUserNicknames
    ) {}
}
