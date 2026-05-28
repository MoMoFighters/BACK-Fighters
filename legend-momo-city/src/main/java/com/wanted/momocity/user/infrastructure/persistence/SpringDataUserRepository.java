package com.wanted.momocity.user.infrastructure.persistence;


import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

public interface SpringDataUserRepository extends JpaRepository<UserJpaEntity, Long> {

    @Modifying
    @Transactional
    @Query("UPDATE UserUser u SET u.nickname = :nickname WHERE u.id = :userId")
    void registerNickname(@Param("userId") Long userId, @Param("nickname") String nickname);

    boolean existsByNickname(String nickname);

    // 값이 있으면 바꾸고 널이면 기본 값 유지
    @Modifying
    @Transactional
    @Query("UPDATE UserUser u SET " +
            "u.nickname = CASE WHEN :nickname IS NOT NULL THEN :nickname ELSE u.nickname END, " +
            "u.profileImageUrl = CASE WHEN :profileImageUrl IS NOT NULL THEN :profileImageUrl ELSE u.profileImageUrl END, " +
            "u.password = CASE WHEN :password IS NOT NULL THEN :password ELSE u.password END " +
            "WHERE u.id = :userId")
    void updateUserInfo(@Param("userId") Long userId,
                        @Param("nickname") String nickname,
                        @Param("profileImageUrl") String profileImageUrl,
                        @Param("password") String password);}
