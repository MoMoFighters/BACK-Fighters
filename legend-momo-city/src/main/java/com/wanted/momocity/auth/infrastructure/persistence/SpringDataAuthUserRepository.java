package com.wanted.momocity.auth.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface SpringDataAuthUserRepository extends JpaRepository<UserJpaEntity, Long> {

    boolean existsByEmail(String email);

    Optional<UserJpaEntity> findByEmail(String email);

    // 임시비밀번호 발급할 때 db에 비밀번호 칼럼을 임시 비밀번호 값으로 업데이트
    @Modifying
    @Query("UPDATE AuthUser u SET u.password = :password, u.isTempPwd = true WHERE u.email = :email")
    void updatePassword(@Param("email") String email, @Param("password") String password);}
