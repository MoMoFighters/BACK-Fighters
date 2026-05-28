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
}
