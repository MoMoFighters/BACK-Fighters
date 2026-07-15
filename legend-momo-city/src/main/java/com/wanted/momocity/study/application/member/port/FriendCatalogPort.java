package com.wanted.momocity.study.application.member.port;

/*
 * comment.
 *  friend 컨텍스트 소유의 친구 관계를 READ 전용으로 조회
 *  - study(member) 컨텍스트 전용 Port
 *  - study가 friend 도메인을 직접 참조하지 않고 이 포트를 통해서만 접근
 *  - 구현체 : FriendCatalogAdapter (infrastructure.catalog)
 *  -
 *  friend 테이블은 단방향 유니크(from_user_id, to_user_id)라 양방향 OR 조회가 필요
 *  (from=A,to=B) 또는 (from=B,to=A) 중 하나만 존재하고 status='FRIEND' 면 친구 관계로 봄
 * */

public interface FriendCatalogPort {

    // 두 유저가 친구 관계(status=FRIEND)인지 확인
    boolean isFriend(Long userId1, Long userId2);

}