package com.wanted.momocity.friend.infrastructure.persistence;

import com.wanted.momocity.friend.user.UserWithFMJpaEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "guestbook")
@NoArgsConstructor
@Getter
public class GuestBookJpaEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "writer_id")
    private UserWithFMJpaEntity writerId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_id")
    private UserWithFMJpaEntity ownerId;

    @Column(name = "content")
    private String content;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    //방명록 작성
    public static GuestBookJpaEntity create(UserWithFMJpaEntity loginUser, UserWithFMJpaEntity ownerUser, String content, LocalDateTime now) {
        GuestBookJpaEntity gb = new GuestBookJpaEntity();
        gb.writerId = loginUser;
        gb.ownerId = ownerUser;
        gb.content = content;
        gb.createdAt = now;

        return gb;
    }
}
