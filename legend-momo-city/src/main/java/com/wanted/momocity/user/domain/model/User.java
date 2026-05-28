package com.wanted.momocity.user.domain.model;

import com.wanted.momocity.auth.domain.model.Category;
import com.wanted.momocity.auth.domain.model.Role;
import com.wanted.momocity.auth.domain.model.Status;
import lombok.Builder;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Builder
public class User {

    private final Long id;
    private final String email;
    private final String password;
    private final String name;
    private final String nickname;
    private final LocalDate birth;
    private final String profileImageUrl;
    private final Role role;          // STUDENT, TEACHER, ADMIN
    private final Status status;      // ACTIVE, SUSPENDED, BANNED, DELETED, BLACK
    private final Category category;  // HEALTH, STUDY, COOK, BEAUTY, ART
    private final String proof;
    private final Long point;
    private final Boolean isPaid;  // 결제를 했는지 안 했는지
    private final Boolean doNotDisturb; // 설정 끄기를 했는지 안 했는지
    private final Instant createdAt;
    private final LocalDateTime updatedAt;
    private final LocalDateTime deletedAt;
    private final Boolean isTempPwd;  // 이 사용자의 비밀번호가 임시비밀번호인지 아닌지

    public User(Long id, String email, String password, String name, String nickname, LocalDate birth, String profileImageUrl, Role role, Status status, Category category, String proof, Long point, Boolean isPaid, Boolean doNotDisturb, Instant createdAt, LocalDateTime updatedAt, LocalDateTime deletedAt, Boolean isTempPwd) {
        this.id = id;
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

    // 마이페이지에서 사용자에게 제시할 사용자 정보가 몇 개 없어서 user칼럼 전체 다 가져오면 널이 너무 만ㅎ음
    // -> 빌더 사용
    public static User restore(String profileImageUrl, String email,
                               String name, String nickname, LocalDate birth) {
        return User.builder()
                .profileImageUrl(profileImageUrl)
                .email(email)
                .name(name)
                .nickname(nickname)
                .birth(birth)
                .build();
    }


    public String getEmail() {
        return email;
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
}
