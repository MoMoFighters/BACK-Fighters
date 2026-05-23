package com.wanted.momocity.auth.domain.model;

import com.wanted.momocity.auth.infrastructure.persistence.UserJpaEntity;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;

public class User {

    private final Long id;
    private final String email;
    private final String password;
    private final String name;
    private final String nickname;
    private final LocalDate birth;
    private final String profileImageUrl;
    private final UserRole role;          // STUDENT, TEACHER, ADMIN
    private final UserStatus status;      // ACTIVE, SUSPENDED, BANNED, DELETED, BLACK
    private final UserCategory category;  // HEALTH, STUDY, COOK, BEAUTY, ART
    private final String proof;
    private final ApplicationStatus applicationStatus; // NONE, PENDING, REJECTED, APPROVED
    private final Long point;
    private final Boolean isPaid;  // 결제를 했는지 안 했는지
    private final Boolean doNotDisturb; // 설정 끄기를 했는지 안 했는지
    private final Instant createdAt;
    private final LocalDateTime updatedAt;
    private final LocalDateTime deletedAt;
    private final Boolean isTempPwd;  // 이 사용자의 비밀번호가 임시비밀번호인지 아닌지

    public User(Long id,  String email, String password, String name, String nickname, LocalDate birth, String profileImageUrl, UserRole role, UserStatus status, UserCategory category, String proof, ApplicationStatus applicationStatus, Long point, Boolean isPaid, Boolean doNotDisturb, Instant createdAt, LocalDateTime updatedAt, LocalDateTime deletedAt, Boolean isTempPwd) {
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
        this.applicationStatus = applicationStatus;
        this.point = point;
        this.isPaid = isPaid;
        this.doNotDisturb = doNotDisturb;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.deletedAt = deletedAt;
        this.isTempPwd = isTempPwd;
    }

    public static User createStudent(String email, String password, String name ) {
        return new User(
                null,           // id,
                email, password, name,
                null,           // nickname
                null,           // birth
                null,           // profileImageUrl
                UserRole.STUDENT,
                UserStatus.ACTIVE,      // 가입 시 기본값
                null,           // category (학생은 null)
                null,           // proof
                ApplicationStatus.NONE, // 가입 시 기본값
                0L,              // point
                false,          // isPaid
                false,          // doNotDisturb
                Instant.now(),    // createdAt
                LocalDateTime.now(),    // updatedAt
                null,           // deletedAt
                false           // isTempPwd
        );
    }

    public Long getUserId() {
        return id;
    }
}
