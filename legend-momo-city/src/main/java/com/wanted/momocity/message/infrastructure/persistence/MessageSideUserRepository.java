package com.wanted.momocity.message.infrastructure.persistence;


import com.wanted.momocity.friend.user.UserWithFMJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

//충돌을 피하기 위해 메시지 기능 관련 사용자 테이블 사용하는 공간
public interface MessageSideUserRepository extends JpaRepository<UserWithFMJpaEntity, Long> {

    //사용자 찾기
    Optional<Object> findUserById(Long senderId);
}
