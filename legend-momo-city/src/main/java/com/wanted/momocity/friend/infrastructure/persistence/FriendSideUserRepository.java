package com.wanted.momocity.friend.infrastructure.persistence;


import com.wanted.momocity.friend.user.UserWithFMJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

//충돌을 피하기 위해 친구 기능 관련 사용자 테이블 사용하는 공간
public interface FriendSideUserRepository extends JpaRepository<UserWithFMJpaEntity, Long> {

    //닉네임 포함 검색
    // 🎯 [최적화] 기존 메서드명 그대로 유지!
    // 만약 User 내부의 프로필이나 특정 객체가 지연로딩이라면 여기서 fetch join을 걸어 한방에 긁어옵니다.
    // (여기서는 @Entity(name) 설정이 따로 없다면 클래스명인 UserWithFMJpaEntity를 그대로 적어줍니다)
    @Query("select u from FMUser u " +
            "where u.nickname like concat('%', :nickname, '%')")
    List<UserWithFMJpaEntity> findByNicknameContaining(@Param("nickname") String nickname);
}
