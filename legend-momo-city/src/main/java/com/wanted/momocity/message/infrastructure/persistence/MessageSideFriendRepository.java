package com.wanted.momocity.message.infrastructure.persistence;

import com.wanted.momocity.friend.infrastructure.persistence.FriendJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
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

    //멤버 초대 개선 보강
    // 2. 나와 초대 대상자들 간의 양방향 친구 관계를 IN 쿼리로 한방에 조회
    @Query("select f from FriendJpaEntity f " +
            "where (f.fromUserId.id = :myId and f.toUserId.id in :targetIds) " +
            "   or (f.toUserId.id = :myId and f.fromUserId.id in :targetIds)")
    List<FriendJpaEntity> findFriendRelationsByUserIdAndTargetIds(
            @Param("myId") Long myId,
            @Param("targetIds") List<Long> targetIds
    );

    //채팅방 목록 조회 개선 보강
    // 2. 목록에 등장하는 모든 상대방 유저들과의 친구 관계를 한방에 로드
    @Query("SELECT f FROM FriendJpaEntity f " +
            "WHERE (f.fromUserId.id = :userId AND f.toUserId.id IN :targetUserIds) " +
            "OR (f.toUserId.id = :userId AND f.fromUserId.id IN :targetUserIds)")
    List<FriendJpaEntity> findFriendRelationsByTargetUserIdsIn(@Param("userId") Long userId, @Param("targetUserIds") Collection<Long> targetUserIds);
}
