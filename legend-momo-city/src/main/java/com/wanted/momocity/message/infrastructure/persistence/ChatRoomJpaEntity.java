package com.wanted.momocity.message.infrastructure.persistence;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "chat_room")
@NoArgsConstructor
@Getter
public class ChatRoomJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "title")
    private String roomTitle;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    //채팅방 신설 시 생성 시간 주입
    public void changeCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    //채팅방 개설 시 업데이트 시간 주입
    public void changeUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    //채팅방 이름 등록
    public void registRoomTitle(String roomTitle) {
        this.roomTitle = roomTitle;
    }
}

