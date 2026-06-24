package com.wanted.momocity.message.infrastructure.persistence;

import com.wanted.momocity.friend.user.UserWithFMJpaEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "message_read")
@NoArgsConstructor
@Getter
public class MessageReadJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "room_id")
    private ChatRoomJpaEntity roomId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "message_id")
    private MessageJpaEntity messageId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private UserWithFMJpaEntity userId;

    @Column(name = "is_msg_read")
    private boolean isMsgRead; //메시지 읽음 여부

    @Column(name = "is_noti_read")
    private boolean isNotiRead; //알림 읽음 여부

    @Column(name = "is_deleted")
    private boolean isDeleted;

    //메시지 읽음 처리
    public void changeIsMsgRead(boolean isMsgRead) {
        this.isMsgRead = isMsgRead;
    }

    //메시지 읽음 시 알림도 읽음 처리
    public void changeIsNotiRead(boolean isNotiRead) {
        this.isNotiRead = isNotiRead;
    }

    //메지시 전송 시 읽음 여부, 알림 삭제 여부 처리
    public static MessageReadJpaEntity createNewUnreadMessage(ChatRoomJpaEntity chatRoom, MessageJpaEntity messageId, UserWithFMJpaEntity userId, boolean isMsgRead, boolean isNotiRead, boolean isDeleted) {
        MessageReadJpaEntity m = new MessageReadJpaEntity();

        m.roomId = chatRoom;
        m.messageId = messageId;
        m.userId = userId;
        m.isMsgRead = isMsgRead;
        m.isNotiRead = isNotiRead;
        m.isDeleted = isDeleted;

        return m;
    }
}
