package com.wanted.momocity.message.infrastructure.persistence;

import com.wanted.momocity.friend.infrastructure.persistence.FriendJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

//충돌을 피하기 위해 메시지 기능 관련 사용자 테이블 사용하는 공간
public interface MessageSideFriendRepository extends JpaRepository<FriendJpaEntity, Long> {
    //두 유저 간의 친구 관계 행 조회(양방향 단건 조회)
    Optional<FriendJpaEntity> findByFromUserId_IdAndToUserId_Id(Long fromUserId, Long toUserId);

}
