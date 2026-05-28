package com.wanted.momocity.friend.infrastructure.catalog;


import com.wanted.momocity.friend.application.usecase.FindUserQueryUseCase.FindView;
import com.wanted.momocity.friend.application.usecase.FriendQueryUseCase.FriendView;
import com.wanted.momocity.friend.domain.repository.FriendRepository;
import com.wanted.momocity.friend.infrastructure.persistence.FriendJpaEntity;
import com.wanted.momocity.friend.infrastructure.persistence.FriendSideEnrollmentRepository;
import com.wanted.momocity.friend.infrastructure.persistence.FriendSideUserRepository;
import com.wanted.momocity.friend.infrastructure.persistence.SpringDataFriendRepository;

import com.wanted.momocity.friend.user.UserWithFMJpaEntity;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import javax.swing.text.html.Option;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

//포트 문을 통해 db세상으로 나가는 문
//다른 테이블에서 필요한 정보 가져오기(또는 서비스에서)
@Repository
@Transactional(readOnly = true)
@RequiredArgsConstructor
@Slf4j
public class CatalogFriendAdapter implements FriendRepository {

    //🚨 기능 구현을 위해 다른 테이블의 JpaEntity를 만들어 진행함에 따라 머지 후 import 필요합니다.


    private final SpringDataFriendRepository springDataFriendRepository;
    //충돌 회피로 만든 수강신청 인터페이스 저장소
    private final FriendSideEnrollmentRepository friendSideEnrollmentRepository;
    //충돌 회피로 만든 사용자 인터페이스 저장소
    private final FriendSideUserRepository friendSideUserRepository;


    //내 친구 목록
    @Override
    public List<FriendJpaEntity> findFriendsByUserIdAndStatus(Long userId, String status) {

        //어떤 유저가 친구 목록 조회를 시작했는지 기록
        log.info("[CatalogFriendAdaptor] 친구 목록 DB 조회 요청 - 유저 ID: {}, 상태: {}", userId, status);

        //가공없이 순수한 JPA 엔티티 리스트만 조회해서 서비스로 던지기
        return springDataFriendRepository.findByFromUserId_IdOrToUserId_IdAndStatus(userId, userId, status);
    }

    //사용자 검색
    @Override
    public List<UserWithFMJpaEntity> findUsersByNicknameKeyword(String findNickname) {
        log.info("[CatalogFriendAdapter] 전체 유저 테이블 키워드 검색 시작 - 검색어: {}", findNickname);
        return friendSideUserRepository.findByNicknameContaining(findNickname);
    }
    //로그인한 유저와 엮인 모든 친구 관계 행 조회(상태 판별을 위해)
    @Override
    public List<FriendJpaEntity> findAllMyRelations(Long userId) {
        log.info("[CatalogFriendAdapter] 로그인한 유저와 연관된 모든 친구 관계 조회 - 유저ID: {}", userId);
        return springDataFriendRepository.findByFromUserId_IdOrToUserId_Id(userId, userId);
    }

    //친구 요청
    //단건 유저 존재 여부 검증
    @Override
    public Optional<UserWithFMJpaEntity> findUserById(Long userId) {
        log.info("[CatalogFriendAdapter] 단건 유저 존재 여부 검증 시도 - 대상 유저ID: {}", userId);
        return friendSideUserRepository.findById(userId);
    }
    //친구 관계 존재 여부 조회
    @Override
    public Optional<FriendJpaEntity> findRelationBetween(Long fromUserId, Long toUserId) {
        log.info("[CatalogFriendAdapter] 친구 요청 관계 존재 여부 조회 시도 - 요청차: {}, 대상자: {}", fromUserId, toUserId);
        return springDataFriendRepository.findByFromUserId_IdAndToUserId_Id(fromUserId, toUserId);
    }
    //friend 테이블에 새로운 관계 저장
    @Override
    @Transactional //읽기 전용 어댑터 기조에서 이 메서드만 데이터 생성하므로 추가
    public FriendJpaEntity saveFriendRelation(FriendJpaEntity friendRelation) {
        log.info("[CatalogFriendAdapter] friend 테이블 새 행 삽입 시도 - 요청차: {}, 대상자: {}, 세팅 상태: {}",
                friendRelation.getFromUserId().getId(),
                friendRelation.getToUserId().getId(),
                friendRelation.getStatus());
        return springDataFriendRepository.save(friendRelation);
    }

    //수강신청 완료 후 friend 테이블에 강사-학생 행 추가
    public void save(FriendJpaEntity newFriendRelation) {
        log.info("[CatalogFriendAdapter] 강사-학생 자동 친구 행 삽입 시도 - 학생: {}, 강사: {}",
                newFriendRelation.getFromUserId().getId(),
                newFriendRelation.getToUserId().getId());
        springDataFriendRepository.save(newFriendRelation);
    }

    //친구 요청 철회
    @Override
    public void delete(FriendJpaEntity friendRelation) {
        log.info("[CatalogFriendAdapter] friend 테이블에서 행 삭제 시도 - ID: {}", friendRelation.getId());
        springDataFriendRepository.delete(friendRelation);
    }

    //보낸 친구 요청 목록
    @Override
    public List<FriendJpaEntity> findSentRequestsByFromUserId(Long userId, String status) {
        log.info("[CatalogFriendAdapter] 보낸 친구 요청 목록 DB 조회 요청 - 유저ID: {}, 상태: {}", userId, status);

        return springDataFriendRepository.findByFromUserId_IdAndStatus(userId, status);
    }

    //받은 친구 요청 목록
    @Override
    public List<FriendJpaEntity> findReceivedRequestsByToUserId(Long userId, String status) {
        log.info("[CatalogFriendAdapter] 받은 친구 요청 목록 DB 조회 요청 - 수신자ID: {}, 상태: {}", userId, status);

        return springDataFriendRepository.findByToUserId_IdAndStatus(userId, status);
    }

    //친구 차단
    @Override
    public Optional<FriendJpaEntity> findAnyRelationBetween(Long userA, Long userB) {
        log.info("[CatalogFriendAdapter] 양방향 친구 관계 존재 여부 조회 시도 - 유저A: {}, 유저B: {}", userA, userB);

        //A->B 방향 조회
        Optional<FriendJpaEntity> forward = springDataFriendRepository.findByFromUserId_IdAndToUserId_Id(userA, userB);
        if (forward.isPresent()) {
            return forward;
        }

        //없으면 B->A 방향 조회해서 반환
        return springDataFriendRepository.findByFromUserId_IdAndToUserId_Id(userB, userA);
    }

}
