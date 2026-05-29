package com.wanted.momocity.auth.infrastructure.persistence;

import com.wanted.momocity.auth.domain.model.Category;
import com.wanted.momocity.auth.domain.model.Role;
import com.wanted.momocity.auth.domain.model.Status;
import jakarta.persistence.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity(name = "AuthUser")
@Table(name="`user`")
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

    @Column(name = "birth")
    private LocalDate birth;

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

    @Column(name = "is_paid")
    private boolean isPaid;

    @Column(name = "do_not_disturb")
    private boolean doNotDisturb;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    @Column(name = "is_tempPWD")
    private boolean isTempPwd;


    protected UserJpaEntity() {}

    public UserJpaEntity(String email, String password, String name, String nickname, LocalDate birth, String profileImageUrl, Role role, Status status, Category category, String proof, Long point, boolean isPaid, boolean doNotDisturb, LocalDateTime createdAt, LocalDateTime updatedAt, LocalDateTime deletedAt, boolean isTempPwd) {
        this.email = email;
        this.password = password;
        this.name = name;
        this.nickname = nickname;
        this.birth = birth;
        this.profileImageUrl = profileImageUrl;
        this.role = role;
        this.status = status;
        this.category = category;
        this.proof = proof;
        this.point = point;
        this.isPaid = isPaid;
        this.doNotDisturb = doNotDisturb;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.deletedAt = deletedAt;
        this.isTempPwd = isTempPwd;
    }


    public Long getId() {
        return id;
    }

    public String getEmail() {
        return email;
    }

    public String getPassword() {
        return password;
    }

    public String getName() {
        return name;
    }

    public String getNickname() {
        return nickname;
    }

    public LocalDate getBirth() {
        return birth;
    }

    public String getProfileImageUrl() {
        return profileImageUrl;
    }

    public Role getRole() {
        return role;
    }

    public Status getStatus() {
        return status;
    }

    public Category getCategory() {
        return category;
    }

    public String getProof() {
        return proof;
    }

    public long getPoint() {
        return point;
    }

    public boolean isPaid() {
        return isPaid;
    }

    public boolean isDoNotDisturb() {
        return doNotDisturb;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public LocalDateTime getDeletedAt() {
        return deletedAt;
    }

    public boolean isTempPwd() {
        return isTempPwd;
    }
}
