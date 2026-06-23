package com.wanted.momocity.message.application.usecase;

import com.wanted.momocity.message.application.query.GetMessageHistoryQuery;
import com.wanted.momocity.message.application.query.FindChatRoomQuery;

import java.time.LocalDateTime;
import java.util.List;

public interface MessageQueryUseCase {

    //메시지 채팅 목록
    List<ChatRoomView> getChatRoomQueryHandle(FindChatRoomQuery query);

    record ChatRoomView(
            RoomInfo roomInfo,
            List<MemberInfo> memberInfo,
            Boolean isNotActive, //ACTIVE아닌 것
            boolean shouldMasked,
            LocalDateTime lastestOrderTime //채팅방 목록 정령을 위함
    ) {}
    record RoomInfo(
            Long roomId,
            String roomTitle,
            Long inMemberCount,
            String content, //마지막 채팅 내역
            LocalDateTime createdAt, //마지막 채팅 시각
            Long unreadCount //채팅방에서 읽지 않은 메시지 총 수
    ) {}
    record MemberInfo(
            Long userId,
            String name, //강사 본명
            String nickname, //가공된 닉네임(알 수 없음)
            List<String> lectureTitle, //가공된 강의명 묶음
            String role,
            String status, //친구 상태
            String profileImageUrl,
            boolean isLeftRoom
    ) {}

    //메시지 내역 조회
    List<MessageHistoryView> getMessageHistoryQueryHandle(GetMessageHistoryQuery query);

    record MessageHistoryView(
            RoomInfoView roomInfo,
            List<MemberInfoView> memberInfo,
            List<MessageDetail> messages,
            boolean isNotActive, //활성 상태 아닌 것
            boolean isRead,
            boolean shouldMasked
    ) {}

    record RoomInfoView(
            Long roomId,
            Long inMemberCount,
            String roomTitle
    ) {}

    record MemberInfoView(
            Long userId,
            String name, //강사일 경우
            String nickname,
            String lectureTitle, //강의명 리스트
            String role, //강사/학생 구분
            String status, //친구 상태
            String profileImageUrl,
            Boolean isLeftRoom
    ) {}

    record MessageDetail(
            Long messageId, //일반 메시지 또는 안내 문구
            Long senderId,
            String name,
            String nickname,
            String role,
            String status,
            String content,
            LocalDateTime createdAt, //메시지 시간 또는 안내 문구
            Long unreadCount, //말풍선에 띄울 해당 메시지를 안읽은 사람 수
            Boolean isMine,
            Boolean isLeftRoom,
            String profileImageUrl,
            Long targetUserId, //안내 문구에 해당하는 사람(ex. 초대된 사람, 나간 사람, 이름 바꾼 사람)
            String type //안내 문구의 유형
    ) {}
}
