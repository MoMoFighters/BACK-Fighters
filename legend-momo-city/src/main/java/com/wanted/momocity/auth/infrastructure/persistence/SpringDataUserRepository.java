package com.wanted.momocity.auth.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface SpringDataUserRepository extends JpaRepository<UserJpaEntity, Long> {

    boolean existsByEmail(String email);

    Optional<UserJpaEntity> findByEmail(String email);

    @Modifying
    @Query("UPDATE UserJpaEntity u SET u.password = :password, u.isTempPwd = true WHERE u.email = :email")
    void updatePassword(@Param("email") String email, @Param("password") String password);}
