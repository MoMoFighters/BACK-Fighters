package com.wanted.momocity.friend.domain.repository;

import com.wanted.momocity.friend.application.usecase.FindUserQueryUseCase.FindView;
import com.wanted.momocity.friend.application.usecase.FriendQueryUseCase.FriendView;
import com.wanted.momocity.friend.infrastructure.persistence.FriendJpaEntity;

import com.wanted.momocity.friend.user.UserWithFMJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

//포트 역할
public interface FriendRepository {

    //내 친구 목록
    List<FriendJpaEntity> findFriendsByUserIdAndStatus(Long userId, String status);

    //사용자 검색 기능
    //사용자 검색을 위한 순수 유저 데이터 가져오기(친구가 아니어도 긁어옴)
    List<UserWithFMJpaEntity> findUsersByNicknameKeyword(String findNickname);

    //로그인한 유저와 관련된 모든 친구 관계 행 가져오기(status 상태용)
    List<FriendJpaEntity> findAllMyRelations(Long userId);

    //친구 요청
    //유저가 진짜 존재하는 지 확인
    Optional<UserWithFMJpaEntity> findUserById(Long userId);
    //이미 친구 관계가 존재하는지 확인(409 방어)
    Optional<FriendJpaEntity> findRelationBetween(Long fromUserId, Long toUserId);
    //friend 테이블에 새로운 관계 저장
    FriendJpaEntity saveFriendRelation(FriendJpaEntity friendRelation);

    //수강신청 완료 후 강사-학생 자동 친구 저장
    void save(FriendJpaEntity newFriendRelation);

    //친구 요청 철회
    void delete(FriendJpaEntity friendRelation);

    //보낸 친구 요청 목록
    List<FriendJpaEntity> findSentRequestsByFromUserId(Long userId, String status);

    //받은 친구 요청 목록
    List<FriendJpaEntity> findReceivedRequestsByToUserId(Long userId, String sent);

    //친구 차단(양방향 조회)
    Optional<FriendJpaEntity> findAnyRelationBetween(Long userA, Long userB);
}
