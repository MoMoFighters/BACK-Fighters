package com.wanted.momocity.auth.domain.model;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class User {

    private final Long id;
    private final String email;
    private final String password;
    private final String name;
    private final String nickname;
    private final String profileImageUrl;
    private final Role role;          // STUDENT, TEACHER, ADMIN
    private final Status status;      // ACTIVE, SUSPENDED, BANNED, DELETED, BLACK
    private final Category category;  // HEALTH, STUDY, COOK, BEAUTY, ART
    private final String proof;
    private final Long point;
    private final Boolean doNotDisturb; // 설정 끄기를 했는지 안 했는지
    private final Long suspensionCount; // 신고를 몇번 받았는지
    private final LocalDateTime suspendedUntil; // 임시 정지 회원이 언제까지 정지인지
    private final LocalDateTime createdAt;
    private final LocalDateTime updatedAt;
    private final LocalDateTime deletedAt;
    private final Boolean isTempPwd;  // 이 사용자의 비밀번호가 임시비밀번호인지 아닌지

    private User(Long id, String email, String password, String name, String nickname, String profileImageUrl, Role role, Status status, Category category, String proof, Long point,  Boolean doNotDisturb, Long suspensionCount, LocalDateTime suspendedUntil, LocalDateTime createdAt, LocalDateTime updatedAt, LocalDateTime deletedAt, Boolean isTempPwd) {
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

    // DB에서 꺼내 쓸 메서드
    public static User restore(Long id, String email, String password, String name, String nickname, String profileImageUrl, Role role, Status status, Category category, String proof, long point, boolean doNotDisturb, Long suspensionCount, LocalDateTime suspendedUntil, LocalDateTime createdAt, LocalDateTime updatedAt, LocalDateTime deletedAt, boolean isTempPwd) {
        return new User(id, email, password, name, nickname, profileImageUrl,
                role, status, category, proof, point,
                 doNotDisturb, suspensionCount, suspendedUntil, createdAt, updatedAt, deletedAt, isTempPwd);
    }


    // getter
    public Long getId() { return id; }
    public String getEmail() { return email; }
    public String getPassword() { return password; }
    public String getName() { return name; }
    public Role getRole() { return role; }



    // 회원가입
    public static User signup(String email, String password, String name ) {
        return new User(
                null,           // id,
                email,
                password,
                name,
                null,           // nickname
                "https://momocity-bucket.s3.ap-northeast-2.amazonaws.com/profile/momoProfile.png", // profileImageUrl
                Role.STUDENT,
                Status.ACTIVE,      // 가입 시 기본값
                null,           // category (학생은 null)
                null,           // proof
                0L,              // point
                false,          // doNotDisturb
                0L,              // 신고횟수
                null,           // 언제가지 신고인지
                LocalDateTime.now(),    // createdAt
                LocalDateTime.now(),    // updatedAt
                null,           // deletedAt
                false           // isTempPwd
        );
    }

    public String getNickname() {
        return nickname;
    }

    public String getProfileImageUrl() {
        return profileImageUrl;
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

    public Boolean getDoNotDisturb() {
        return doNotDisturb;
    }

    public Long getSuspensionCount() {
        return suspensionCount;
    }

    public LocalDateTime getSuspendedUntil() {
        return suspendedUntil;
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

    public Boolean getIsTempPwd() {
        return isTempPwd;
    }

    // 소셜 로그인 할 때 user 등록
    public static User oAuthRegister(String email, String name) {
        return new User(
                null,           // id
                email,          // 카카오는 이메일 안 받아와서 null 가능
                null,           // password (소셜 로그인은 없음)
                name,
                null,  // nickname
                "https://momocity-bucket.s3.ap-northeast-2.amazonaws.com/profile/momoProfile.png", // profileImageUrl
                Role.STUDENT,   // 기본값
                Status.ACTIVE,  // 가입 시 기본값
                null,           // category
                null,           // proof
                0L,             // point
                false,          // doNotDisturb
                0L,             // 신고횟수
                null,           // 정지 기간
                LocalDateTime.now(),  // createdAt
                LocalDateTime.now(), // updatedAt
                null,           // deletedAt
                false           // isTempPwd
        );
    }
}
