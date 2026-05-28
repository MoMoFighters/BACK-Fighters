package com.wanted.momocity.message.infrastructure.persistence;

import com.wanted.momocity.friend.user.UserWithFMJpaEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "chat_room_member")
@NoArgsConstructor
@Getter
public class ChatRoomMemberJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "room_id")
    private ChatRoomJpaEntity roomId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private UserWithFMJpaEntity userId;

    @Column(name = "joined_at")
    private LocalDateTime joinedAt;


    //채팅방 조회 및 개설(신설)
    public static ChatRoomMemberJpaEntity createMembership(ChatRoomJpaEntity room, UserWithFMJpaEntity userId) {
        ChatRoomMemberJpaEntity membership = new ChatRoomMemberJpaEntity();
        membership.roomId = room; //새로 개설된 방 객체 연결

        membership.userId = userId;

        membership.joinedAt = LocalDateTime.now();
        return membership;
    }
}
