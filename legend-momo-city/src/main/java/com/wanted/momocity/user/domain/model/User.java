package com.wanted.momocity.user.domain.model;

import lombok.Builder;

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
    private final LocalDateTime createdAt;
    private final LocalDateTime updatedAt;
    private final LocalDateTime deletedAt;
    private final Boolean isTempPwd;  // 이 사용자의 비밀번호가 임시비밀번호인지 아닌지

    public User(Long id, String email, String password, String name, String nickname, LocalDate birth, String profileImageUrl, Role role, Status status, Category category, String proof, Long point, Boolean isPaid, Boolean doNotDisturb, LocalDateTime createdAt, LocalDateTime updatedAt, LocalDateTime deletedAt, Boolean isTempPwd) {
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
                               String name, String nickname, LocalDate birth, Boolean isTempPwd, LocalDateTime createdAt) {
        return User.builder()
                .profileImageUrl(profileImageUrl)
                .email(email)
                .name(name)
                .nickname(nickname)
                .birth(birth)
                .isTempPwd(isTempPwd)
                .createdAt(createdAt)
                .build();
    }

    // 광리자 대시보드용 사용자 정보 덩어리 가져오기
    public static User restoreForAdmin(Long id, String name, Role role, String email,
                                       LocalDateTime createdAt, LocalDateTime deletedAt,
                                       Status status, String proof) {
        return User.builder()
                .id(id)
                .name(name)
                .role(role)
                .email(email)
                .createdAt(createdAt)
                .deletedAt(deletedAt)
                .status(status)
                .proof(proof)
                .build();
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

    public Long getPoint() {
        return point;
    }

    public Boolean getPaid() {
        return isPaid;
    }

    public Boolean getDoNotDisturb() {
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

    public Boolean getTempPwd() {
        return isTempPwd;
    }

}
