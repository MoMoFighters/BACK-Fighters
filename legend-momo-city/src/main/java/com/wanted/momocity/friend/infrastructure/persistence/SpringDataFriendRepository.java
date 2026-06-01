package com.wanted.momocity.friend.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SpringDataFriendRepository extends JpaRepository<FriendJpaEntity, Long> {
    //로그인한 유저가 신청/받았고 상태가 "FRIEND"인 것 조회
    List<FriendJpaEntity> findByFromUserId_IdOrToUserId_IdAndStatus(
            Long fromUserId, Long toUserId, String status
    );

    //사용자 검색용 상태 반환을 위함
    List<FriendJpaEntity> findByFromUserId_IdOrToUserId_Id(Long userId, Long userId1);

    //친구 요청(이미 친구 관계인지 확인)
    Optional<FriendJpaEntity> findByFromUserId_IdAndToUserId_Id(Long fromUserId, Long toUserId);

    //보낸 친구 요청 목록
    List<FriendJpaEntity> findByFromUserId_IdAndStatus(Long fromUserId, String status);

    //받은 친구 요청 목록
    List<FriendJpaEntity> findByToUserId_IdAndStatus(Long toUserId, String status);

}
