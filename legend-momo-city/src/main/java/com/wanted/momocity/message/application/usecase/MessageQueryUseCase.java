package com.wanted.momocity.message.application.usecase;

import java.time.LocalDateTime;
import java.util.List;

public interface MessageQueryUseCase {

    //메시지 채팅 목록
    List<ChatRoomView> getChatRoomQueryHandle(Long userId);

    record ChatRoomView(
            Long userId,
            String name, //강사 본명
            String nickname, //가공된 닉네임(알 수 없음)
            String role,
            String status, //친구 상태
            Boolean isNotActive, //ACTIVE아닌 것
            Long roomId, //채팅방 번호
            String content, //마지막 채팅 내역
            LocalDateTime createdAt, //마지막 채팅 시각
            Long unreadCount, //읽지 않은 메시지 수
            List<String> lectureTitle, //가공된 강의명 묶음
            String profileImageUrl
    ) {}

    //메시지 내역 조회
    List<MessageHistoryView> getMessageHistoryQueryHandle(Long roomId, Long userId, Long lastMessageId);

    record MessageHistoryView(
            Long messageId,
            Long senderId,
            String name, //강사일 경우
            String nickname,
            String role, //강사/학생 구분
            String status, //친구 상태
            boolean isNotActive, //활성 상태 아닌 것
            List<String> lectureTitle, //강의명 리스트
            String content,
            LocalDateTime createdAt,
            boolean isRead,
            boolean isMine,
            String profileImageUrl
    ) {}
}
