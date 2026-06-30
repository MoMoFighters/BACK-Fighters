package com.wanted.momocity.message.infrastructure.persistence;


import com.wanted.momocity.friend.user.UserWithFMJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

//충돌을 피하기 위해 메시지 기능 관련 사용자 테이블 사용하는 공간
public interface MessageSideUserRepository extends JpaRepository<UserWithFMJpaEntity, Long> {

    //개선
    // 🎯 [패치 조인 적용] 단건 조회가 아니라 내부 연관 필드까지 한방에 퍼올리도록 튜닝합니다.
    @Query("select u from FMUser u " +
            "where u.id = :userId")
    Optional<UserWithFMJpaEntity> findUserWithFMById(@Param("userId") Long userId);

    //사용자 찾기
    Optional<Object> findUserById(Long senderId);
}
