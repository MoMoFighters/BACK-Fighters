package com.wanted.momocity.message.domain.model;

import com.wanted.momocity.friend.domain.model.FriendStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDate;
import java.time.LocalDateTime;

//도메인 애그리거트
@AllArgsConstructor
@Getter
public class Message {
    private final Long id;
    private final Long roomId;
    private final Long senderId;
    private final String content;
    private final Boolean isRead;
    private final LocalDateTime createdAt;

    //새 메시지 발송할 때의 도메인 생성 규칙
    public static Message create(Long roomId, Long senderId, String content) {
        return new Message(null, roomId, senderId, content, false, LocalDateTime.now());
    }

}
