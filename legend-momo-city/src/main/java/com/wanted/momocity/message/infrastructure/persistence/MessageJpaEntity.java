package com.wanted.momocity.message.infrastructure.persistence;

import com.wanted.momocity.friend.user.UserWithFMJpaEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "message")
@NoArgsConstructor
@Getter
public class MessageJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "room_id")
    private ChatRoomJpaEntity roomId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sender_id")
    private UserWithFMJpaEntity senderId;

    @Column(name = "content")
    private String content;

    @Column(name = "is_read")
    private Boolean isRead;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;


    //채팅 목록 조회 시 마지막 메지지 없을 때
    public void changeContent(String fakeLastMessage) {
        this.content = fakeLastMessage;
    }

    //채팅 목록 조회 시 마지막 메시지의 시간 없을 때
    public void changeCreatedAt(LocalDateTime fakeCreatedAt) {
        this.createdAt = fakeCreatedAt;
    }

    //메시지 전송
    public static MessageJpaEntity createNewMessage(ChatRoomJpaEntity roomId, UserWithFMJpaEntity senderId, String content, boolean isRead) {
        MessageJpaEntity entity = new MessageJpaEntity();
        entity.roomId = roomId;
        entity.senderId = senderId;
        entity.content = content;
        entity.isRead = isRead;
        entity.createdAt = LocalDateTime.now();
        entity.updatedAt = LocalDateTime.now();
        return entity;
    }

    //메시지 읽음 처리
    public void changeIsRead(boolean isRead) {
        this.isRead = isRead;
    }
}
