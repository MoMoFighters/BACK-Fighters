package com.wanted.momocity.friend.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface SpringDataFriendRepository extends JpaRepository<FriendJpaEntity, Long> {
    //로그인한 유저가 신청/받았고 상태가 "FRIEND"인 것 조회
    // [최적화] 기존 메서드명 그대로 유지하고, 위에 fetch join 쿼리만 매핑!
    @Query("select f from FriendJpaEntity f " +
            "join fetch f.fromUserId " +
            "join fetch f.toUserId " +
            "where (f.fromUserId.id = :fromUserId or f.toUserId.id = :toUserId) " +
            "and f.status = :status")
    List<FriendJpaEntity> findByFromUserId_IdOrToUserId_IdAndStatus(
            @Param("fromUserId") Long fromUserId,
            @Param("toUserId") Long toUserId,
            @Param("status") String status
    );

    //사용자 검색용 상태 반환을 위함
    // [최적화] 기존 메서드명 그대로 유지하고, @Query로 연관 유저 객체(from/to)들을 한방에 fetch join!
    @Query("select f from FriendJpaEntity f " +
            "join fetch f.fromUserId " +
            "join fetch f.toUserId " +
            "where f.fromUserId.id = :userId or f.toUserId.id = :userId1")
    List<FriendJpaEntity> findByFromUserId_IdOrToUserId_Id(@Param("userId") Long userId, @Param("userId1") Long userId1);

    //친구 요청(이미 친구 관계인지 확인)
    // [최적화] 친구 요청 시 이미 존재하는 관계인지 확인 (연관 유저 한방에 패치 조인!)
    @Query("select f from FriendJpaEntity f " +
            "join fetch f.fromUserId " +
            "join fetch f.toUserId " +
            "where f.fromUserId.id = :fromUserId " +
            "and f.toUserId.id = :toUserId")
    Optional<FriendJpaEntity> findByFromUserId_IdAndToUserId_Id(@Param("fromUserId") Long fromUserId,
                                                                @Param("toUserId") Long toUserId);

    //보낸 친구 요청 목록
    // [최적화] 기존 메서드명 그대로 유지하고 보낸 요청을 가져올 때 연관 유저(from/to) 한방에 패치 조인!
    @Query("select f from FriendJpaEntity f " +
            "join fetch f.fromUserId " +
            "join fetch f.toUserId " +
            "where f.fromUserId.id = :fromUserId " +
            "and f.status = :status")
    List<FriendJpaEntity> findByFromUserId_IdAndStatus(@Param("fromUserId") Long fromUserId,
                                                       @Param("status") String status);

    //받은 친구 요청 목록
    // [최적화] 기존 메서드명 그대로 유지하고, 받은 요청을 가져올 때 연관 유저(from/to) 한방에 패치 조인!
    @Query("select f from FriendJpaEntity f " +
            "join fetch f.fromUserId " +
            "join fetch f.toUserId " +
            "where f.toUserId.id = :toUserId " +
            "and f.status = :status")
    List<FriendJpaEntity> findByToUserId_IdAndStatus(@Param("toUserId") Long toUserId,
                                                     @Param("status") String status);


    // [성능 최적화] 친구 차단용 양방향 조회 시, 연관 유저 객체들을 단 1방의 쿼리로 FETCH JOIN
    @Query("select f from FriendJpaEntity f " +
            "join fetch f.fromUserId " +
            "join fetch f.toUserId " +
            "where (f.fromUserId.id = :userA and f.toUserId.id = :userB) " +
            "or (f.fromUserId.id = :userB and f.toUserId.id = :userA)")
    Optional<FriendJpaEntity> findAnyRelationBetweenWithUsers(@Param("userA") Long userA, @Param("userB") Long userB);
}
