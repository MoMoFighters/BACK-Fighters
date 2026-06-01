package com.wanted.momocity.friend.infrastructure.persistence;


import com.wanted.momocity.friend.user.UserWithFMJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

//충돌을 피하기 위해 친구 기능 관련 사용자 테이블 사용하는 공간
public interface FriendSideUserRepository extends JpaRepository<UserWithFMJpaEntity, Long> {

    //닉네임 포함 검색
    List<UserWithFMJpaEntity> findByNicknameContaining(String nickname);
}
