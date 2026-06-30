package com.wanted.momocity.message.infrastructure.persistence;

import com.wanted.momocity.friend.infrastructure.persistence.FriendJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

//충돌을 피하기 위해 메시지 기능 관련 사용자 테이블 사용하는 공간
public interface MessageSideFriendRepository extends JpaRepository<FriendJpaEntity, Long> {
    //두 유저 간의 친구 관계 행 조회(양방향 단건 조회)
    // 🎯 [패치 조인 적용] 두 유저 간의 친구 관계 행을 조회할 때,
    // 발신 유저(fromUserId)와 수신 유저(toUserId) 정보까지 한방에 패치 조인으로 묶어서 가져옵니다.
    @Query("select f from FriendJpaEntity f " +
            "join fetch f.fromUserId " +
            "join fetch f.toUserId " +
            "where f.fromUserId.id = :fromUserId and f.toUserId.id = :toUserId")
    Optional<FriendJpaEntity> findByFromUserId_IdAndToUserId_Id(@Param("fromUserId") Long fromUserId,
                                                                @Param("toUserId") Long toUserId);

}
