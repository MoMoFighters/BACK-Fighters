package com.wanted.momocity.message.infrastructure.persistence;

import com.wanted.momocity.friend.user.UserWithFMJpaEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "message_announce")
@NoArgsConstructor
@Getter
public class MessageAnnounceJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "room_id")
    private ChatRoomJpaEntity roomId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "target_id")
    private UserWithFMJpaEntity targetId;

    @Column(name = "content")
    private String content;

    @Column(name = "type")
    private String type;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    //채팅방 나가기 안내 문구 저장
    public static MessageAnnounceJpaEntity createAnnounce(ChatRoomJpaEntity chatRoom, UserWithFMJpaEntity targetId, String content, String type, LocalDateTime createdAt) {
        MessageAnnounceJpaEntity a = new MessageAnnounceJpaEntity();
        a.roomId = chatRoom;
        a.targetId = targetId;
        a.content = content;
        a.type = type;
        a.createdAt = createdAt;

        return a;
    }
}
