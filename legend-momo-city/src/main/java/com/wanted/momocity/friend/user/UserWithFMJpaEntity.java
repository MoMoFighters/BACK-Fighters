package com.wanted.momocity.friend.user;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Entity(name = "FMUser")
@Table(name = "user")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
//: db 오염을 막기 위해 new로 생성 못하게 보호
@Getter
public class UserWithFMJpaEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "email")
    private String email;

    @Column(name = "password")
    private String password;

    @Column(name = "name")
    private String name;

    @Column(name = "nickname")
    private String nickname;

    @Column(name = "profile_image_url")
    private String profileImageUrl;

    @Column(name = "role")
    private String role;

    @Column(name = "status")
    private String status;

}
