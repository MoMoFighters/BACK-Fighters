package com.wanted.momocity.user.infrastructure.persistence;

import com.wanted.momocity.global.domain.model.Category;
import com.wanted.momocity.user.domain.model.Role;
import com.wanted.momocity.user.domain.model.Status;
import com.wanted.momocity.user.domain.model.User;
import jakarta.persistence.*;
import lombok.Getter;

import java.time.LocalDateTime;

@Entity(name = "UserUser")
@Table(name="`user`")
@Getter
public class UserJpaEntity {

    // 직접 user 테이블을 다루는 엔티티 클래스
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = true, unique = true)
    private String email;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false)
    private String name;

    @Column
    private String nickname;

    @Column(name = "profile_image_url")
    private String profileImageUrl;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Status status;

    @Enumerated(EnumType.STRING)
    private Category category;

    @Column
    private String proof;

    @Column(nullable = true)
    private Long point;

    @Column(name = "do_not_disturb")
    private boolean doNotDisturb;

    @Column(name = "suspension_count")
    private Long suspensionCount;

    @Column(name = "suspended_until")
    private LocalDateTime suspendedUntil;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    @Column(name = "is_tempPWD")
    private boolean isTempPwd;


    protected UserJpaEntity() {
    }

    // id 포함 생성자 추가
    public UserJpaEntity(Long id, String email, String password, String name, String nickname, String profileImageUrl, Role role, Status status, Category category, String proof, Long point, boolean doNotDisturb, Long suspensionCount, LocalDateTime suspendedUntil, LocalDateTime createdAt, LocalDateTime updatedAt, LocalDateTime deletedAt, boolean isTempPwd) {
        this.id = id;
        this.email = email;
        this.password = password;
        this.name = name;
        this.nickname = nickname;
        this.profileImageUrl = profileImageUrl;
        this.role = role;
        this.status = status;
        this.category = category;
        this.proof = proof;
        this.point = point;
        this.doNotDisturb = doNotDisturb;
        this.suspensionCount = suspensionCount;
        this.suspendedUntil = suspendedUntil;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.deletedAt = deletedAt;
        this.isTempPwd = isTempPwd;
    }


    // fromDomain
    public static UserJpaEntity fromDomain(User user) {
        return new UserJpaEntity(
                user.getId(),
                user.getEmail(),
                user.getPassword(),
                user.getName(),
                user.getNickname(),
                user.getProfileImageUrl(),
                user.getRole(),
                user.getStatus(),
                user.getCategory(),
                user.getProof(),
                user.getPoint(),
                user.getDoNotDisturb(),
                user.getSuspensionCount(),
                user.getSuspendedUntil(),
                user.getCreatedAt(),
                user.getUpdatedAt(),
                user.getDeletedAt(),
                user.getTempPwd()
        );
    }

    // 포인트 차감
    public void decreasePoint(Long amount) {
        this.point -= amount;
    }

    // 포인트 추가
    public void increasePoint(Long amount) {
        this.point += amount;
    }
}