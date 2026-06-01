package com.wanted.momocity.message.application.usecase;

import java.time.LocalDateTime;
import java.util.List;

public interface GetMessageHistoryQueryUseCase {
    List<MessageHistoryView> handle(Long roomId, Long userId, Long lastMessageId);

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
            String profileImageUrl,
            String notMeTargetName,
            String notMeNickname,
            String notMeRole
    ) {}
}
